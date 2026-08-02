package com.vzap.trytons.service.tournament;

import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamDAO;
import com.vzap.trytons.dao.fixture.FantasyRoundDAO;
import com.vzap.trytons.dao.fixture.FixtureDAO;
import com.vzap.trytons.dao.leaderboard.LeaderboardDAO;
import com.vzap.trytons.dao.league.LeagueDAO;
import com.vzap.trytons.dao.league.LeagueMembershipDAO;
import com.vzap.trytons.dao.results.MatchResultDAO;
import com.vzap.trytons.dao.tournament.TournamentDAO;
import com.vzap.trytons.dao.tournament.TournamentPoolDAO;
import com.vzap.trytons.dao.tournament.TournamentSettingsDAO;
import com.vzap.trytons.dao.tournament.TournamentStandingDAO;
import com.vzap.trytons.dto.tournament.*;
import com.vzap.trytons.enums.*;
import com.vzap.trytons.exceptions.*;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.fixture.FantasyRound;
import com.vzap.trytons.model.fixture.Fixture;
import com.vzap.trytons.model.leaderboard.Leaderboard;
import com.vzap.trytons.model.leaderboard.Ranking;
import com.vzap.trytons.model.league.League;
import com.vzap.trytons.model.league.LeagueMembership;
import com.vzap.trytons.model.results.MatchResult;
import com.vzap.trytons.model.tournament.*;
import com.vzap.trytons.service.shared.SeasonResolver;
import com.vzap.trytons.util.LeagueVisibility;
import com.vzap.trytons.util.tournament.KnockoutBracket;
import com.vzap.trytons.util.tournament.MatchDayEditRules;
import com.vzap.trytons.util.tournament.MatchdayCalendar;
import com.vzap.trytons.util.tournament.PoolAllocator;
import com.vzap.trytons.util.tournament.PoolStandingsCalculator;
import com.vzap.trytons.util.tournament.RoundRobinScheduler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates and runs Rugby World Cup style tournaments.
 *
 * <p>Starting a league draws balanced pools and writes the whole pool stage at
 * once. From then on the tournament advances itself: every time a fantasy
 * round is processed, pool tables are recomputed, a finished pool stage
 * produces the knockout bracket, and each finished knockout round produces the
 * next until a champion is crowned.
 *
 * <p>The sporting rules themselves live in the pure utilities under
 * {@code util.tournament}, which keeps them unit testable; this class is the
 * orchestration and persistence around them.
 */
@ApplicationScoped
public class TournamentServiceImpl implements TournamentService {

    private static final Logger LOG = Logger.getLogger(TournamentServiceImpl.class.getName());

    /**
     * How many times a round insert may re-read the season's highest round
     * number and try again. Rounds are minted per league now, so two leagues
     * starting at the same moment genuinely race for the next number where
     * before they simply shared a round.
     */
    private static final int ROUND_NUMBER_ATTEMPTS = 5;

    @Inject
    private TournamentDAO tournamentDAO;
    @Inject
    private TournamentPoolDAO tournamentPoolDAO;
    @Inject
    private TournamentStandingDAO tournamentStandingDAO;
    @Inject
    private TournamentSettingsDAO tournamentSettingsDAO;
    @Inject
    private LeagueDAO leagueDAO;
    @Inject
    private LeagueMembershipDAO leagueMembershipDAO;
    @Inject
    private FixtureDAO fixtureDAO;
    @Inject
    private FantasyRoundDAO fantasyRoundDAO;
    @Inject
    private MatchResultDAO matchResultDAO;
    @Inject
    private FantasyTeamDAO fantasyTeamDAO;
    @Inject
    private LeaderboardDAO leaderboardDAO;
    @Inject
    private UserDAO userDAO;
    @Inject
    private SeasonResolver seasonResolver;

    // ------------------------------------------------------------------
    // Starting a league
    // ------------------------------------------------------------------

    @Override
    public StartLeagueResponseDTO startLeague(UUID actorUserId, UUID leagueId) {
        if (leagueId == null) {
            throw new ValidationException("A league is required to start a tournament.");
        }

        League league = leagueDAO.findLeagueById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found"));

        requireCanStartLeague(actorUserId, league);

        if (league.getStatus() != null && league.getStatus() != LeagueStatus.FORMING) {
            throw new BusinessRuleException("This league has already been started.");
        }

        List<LeagueMembership> members = leagueMembershipDAO.findActiveByLeague(leagueId);
        int managerCount = members.size();

        if (managerCount < PoolAllocator.MIN_MANAGERS) {
            throw new BusinessRuleException(
                    "A tournament needs at least " + PoolAllocator.MIN_MANAGERS
                            + " managers, but this league has " + managerCount + ".");
        }
        if (managerCount > PoolAllocator.MAX_MANAGERS) {
            throw new BusinessRuleException(
                    "A tournament supports at most " + PoolAllocator.MAX_MANAGERS
                            + " managers, but this league has " + managerCount + ".");
        }

        String season = seasonResolver.resolveCurrentSeason();
        if (tournamentDAO.findByLeagueAndSeason(leagueId, season).isPresent()) {
            throw new ConflictException("This league already has a tournament for the " + season + " season.");
        }

        TournamentSettings settings = loadSettings();

        List<UUID> seededTeams = seedTeams(members, season);
        List<Integer> poolSizes = PoolAllocator.poolSizes(managerCount);
        List<List<UUID>> draw = PoolAllocator.draw(seededTeams);

        int poolMatchdays = poolSizes.stream()
                .mapToInt(RoundRobinScheduler::matchdaysFor)
                .max()
                .orElseThrow();
        int bracketSize = KnockoutBracket.bracketSize(poolSizes.size(), managerCount);

        // A third-place playoff only means anything once there are losing semi-finalists.
        boolean thirdPlacePlayoff = settings.isThirdPlacePlayoff() && bracketSize >= 4;

        Tournament tournament = tournamentDAO.create(Tournament.builder()
                .tournamentId(UUID.randomUUID())
                .leagueId(leagueId)
                .season(season)
                .status(TournamentStatus.POOL_STAGE)
                .managerCount(managerCount)
                .poolCount(poolSizes.size())
                .poolMatchdays(poolMatchdays)
                .bracketSize(bracketSize)
                .thirdPlacePlayoff(thirdPlacePlayoff)
                .build());

        int fixturesGenerated;
        try {
            fixturesGenerated = generatePoolStage(league, tournament, seededTeams, draw);
            leagueDAO.updateStatus(leagueId, LeagueStatus.IN_PROGRESS, LocalDateTime.now());
        } catch (RuntimeException e) {
            // Generation spans many independent statements rather than one
            // database transaction, so a failure part way through would
            // otherwise strand a half-drawn competition. Deleting the
            // tournament cascades to its pools, members, standings and any
            // fixtures already written, leaving the league startable again.
            LOG.log(Level.SEVERE,
                    "Rolling back partially generated tournament for league " + leagueId, e);
            try {
                tournamentDAO.delete(tournament.getTournamentId());
            } catch (RuntimeException cleanupFailure) {
                e.addSuppressed(cleanupFailure);
            }
            throw e;
        }

        int totalMatchdays = poolMatchdays + KnockoutBracket.knockoutRounds(bracketSize);

        return StartLeagueResponseDTO.builder()
                .leagueId(leagueId)
                .tournamentId(tournament.getTournamentId())
                .season(season)
                .managerCount(managerCount)
                .poolCount(poolSizes.size())
                .poolMatchdays(poolMatchdays)
                .bracketSize(bracketSize)
                .totalMatchdays(totalMatchdays)
                // Matchday one only. The pool stage is generated a matchday at
                // a time now, so this is not the whole tournament's fixture
                // count and must not be reported as one.
                .fixturesGenerated(fixturesGenerated)
                .message(managerCount + " managers drawn into " + poolSizes.size()
                        + " pool(s). " + fixturesGenerated + " fixtures scheduled for matchday 1 of "
                        + poolMatchdays + "; each following matchday is scheduled once the previous "
                        + "one has been played, then a " + bracketSize + "-team knockout stage.")
                .build();
    }

    /**
     * Writes the pools, their members and empty standings rows, then schedules
     * the first pool matchday. Split out from {@link #startLeague} so a failure
     * can be undone as a unit.
     *
     * <p>Only matchday one is written. The rest of the pool stage is generated
     * a matchday at a time from {@link #advanceTournament}, exactly as the
     * knockout bracket already was, so a league that is never played does not
     * leave a run of dated-but-dead fixtures behind it and every matchday is
     * scheduled relative to when the previous one actually finished.
     *
     * @return how many fixtures were generated for matchday one
     */
    private int generatePoolStage(League league,
                                  Tournament tournament,
                                  List<UUID> seededTeams,
                                  List<List<UUID>> draw) {
        Map<UUID, Integer> seedByTeam = new HashMap<>();
        for (int i = 0; i < seededTeams.size(); i++) {
            seedByTeam.put(seededTeams.get(i), i + 1);
        }

        for (int poolIndex = 0; poolIndex < draw.size(); poolIndex++) {
            List<UUID> poolTeams = draw.get(poolIndex);

            TournamentPool pool = tournamentPoolDAO.create(TournamentPool.builder()
                    .poolId(UUID.randomUUID())
                    .tournamentId(tournament.getTournamentId())
                    .poolName(PoolAllocator.poolName(poolIndex))
                    .poolSize(poolTeams.size())
                    .build());

            for (UUID teamId : poolTeams) {
                tournamentPoolDAO.addMember(TournamentPoolMember.builder()
                        .poolMemberId(UUID.randomUUID())
                        .tournamentId(tournament.getTournamentId())
                        .poolId(pool.getPoolId())
                        .teamId(teamId)
                        .seed(seedByTeam.get(teamId))
                        .build());

                // Start every manager on an empty row so the table is complete
                // before a single fixture has been played.
                tournamentStandingDAO.create(TournamentStanding.builder()
                        .standingId(UUID.randomUUID())
                        .tournamentId(tournament.getTournamentId())
                        .poolId(pool.getPoolId())
                        .teamId(teamId)
                        .build());
            }
        }

        // A league is started now, so the first matchday is measured from now
        // -- or from startedAt if that is somehow later. The old code took
        // whichever UPCOMING round had the lowest number regardless of date,
        // which is how a league started on 2026-08-02 acquired a fixture dated
        // 2026-07-24.
        LocalDateTime anchor = latest(LocalDateTime.now(), league.getStartedAt());
        int fixturesGenerated = generatePoolMatchday(tournament, 1, anchor);

        // Seed the tables so a freshly drawn pool reads as a proper standings
        // table -- everyone on nothing, ordered by draw seed -- rather than an
        // unordered list with no positions until the first result lands.
        refreshStandings(tournament);

        return fixturesGenerated;
    }

    /**
     * Schedules one pool matchday: a freshly minted fantasy round, and the
     * fixtures every pool plays on it.
     *
     * <p>The draw order within a pool is recoverable from the stored seeds --
     * {@link PoolAllocator#draw} fills each pool in seed order -- and
     * {@link RoundRobinScheduler} is pure, so matchday {@code n} can be
     * rederived on demand rather than having to be written out up front.
     *
     * @param matchdayNumber one based
     * @param anchor         nothing is scheduled at or before this instant
     * @return how many fixtures were created
     */
    private int generatePoolMatchday(Tournament tournament, int matchdayNumber, LocalDateTime anchor) {
        List<TournamentPool> pools = tournamentPoolDAO.findByTournament(tournament.getTournamentId());
        if (pools.isEmpty()) {
            return 0;
        }

        // Work the pairings out before minting the round, so a pool that
        // cannot be scheduled does not leave an empty fantasy round behind.
        Map<UUID, List<RoundRobinScheduler.Pairing<UUID>>> pairingsByPool = new LinkedHashMap<>();
        int expected = 0;
        for (TournamentPool pool : pools) {
            List<TournamentPoolMember> members =
                    new ArrayList<>(tournamentPoolDAO.findMembersByPool(pool.getPoolId()));
            members.sort(Comparator.comparingInt(TournamentPoolMember::getSeed));

            List<UUID> poolTeams = new ArrayList<>();
            for (TournamentPoolMember member : members) {
                poolTeams.add(member.getTeamId());
            }
            if (poolTeams.size() < 2) {
                continue;
            }

            // A pool of three plays as many matchdays as a pool of four, but an
            // out-of-range index simply yields no fixtures rather than throwing.
            List<RoundRobinScheduler.Pairing<UUID>> pairings =
                    RoundRobinScheduler.matchday(poolTeams, matchdayNumber - 1);
            pairingsByPool.put(pool.getPoolId(), pairings);
            expected += pairings.size();
        }

        if (expected == 0) {
            return 0;
        }

        FantasyRound round = mintRounds(tournament.getSeason(), anchor, 1).get(0);

        int fixturesGenerated = 0;
        for (Map.Entry<UUID, List<RoundRobinScheduler.Pairing<UUID>>> entry : pairingsByPool.entrySet()) {
            for (RoundRobinScheduler.Pairing<UUID> pairing : entry.getValue()) {
                createFixture(tournament.getLeagueId(), tournament, round, pairing,
                        TournamentStage.POOL, entry.getKey(), null, matchdayNumber);
                fixturesGenerated++;
            }
        }
        return fixturesGenerated;
    }

    /**
     * Orders managers strongest first so the snake draft can spread them
     * evenly. Season form from the master leaderboard is the natural measure;
     * a league whose managers have not played yet falls back to join order,
     * which at least keeps the draw deterministic.
     */
    private List<UUID> seedTeams(List<LeagueMembership> members, String season) {
        Map<UUID, Integer> pointsByTeam = new HashMap<>();

        Optional<Leaderboard> master = leaderboardDAO.getMasterLeaderboard(season);
        if (master.isPresent()) {
            for (Ranking ranking : leaderboardDAO.getRankingsByLeaderboardId(master.get().getLeaderboardId())) {
                pointsByTeam.put(ranking.getTeamId(), ranking.getTotalFantasyPoints());
            }
        }

        List<LeagueMembership> ordered = new ArrayList<>(members);
        ordered.sort(Comparator
                .comparingInt((LeagueMembership membership) ->
                        pointsByTeam.getOrDefault(membership.getTeamId(), 0)).reversed()
                .thenComparing(membership -> membership.getJoinDate() == null
                        ? LocalDateTime.MIN : membership.getJoinDate())
                .thenComparing(membership -> membership.getTeamId().toString()));

        List<UUID> seeded = new ArrayList<>();
        for (LeagueMembership membership : ordered) {
            seeded.add(membership.getTeamId());
        }
        return seeded;
    }

    /**
     * Creates {@code count} brand new fantasy rounds, the first kicking off
     * strictly after {@code from}.
     *
     * <p>This deliberately does not look at existing rounds. The old
     * {@code allocateRounds} claimed whichever UPCOMING round had the lowest
     * number, with no check that its date was in the future -- which is how a
     * league started on 2026-08-02 was handed a fixture dated 2026-07-24. A
     * round's dates, not its number, decide when it is played, so scheduling
     * now mints its own rounds from {@link MatchdayCalendar} and never reuses
     * one.
     *
     * <p>A second benefit: every minted round is used by exactly one league on
     * exactly one matchday, so {@code trg_fixture_integrity_insert}'s rule that
     * a fantasy team may appear only once per league round cannot fire during
     * generation.
     *
     * <p>roundNumber remains the season-wide sequence, and that is now a
     * genuinely contended value -- two leagues starting at the same instant
     * both want the next number, where before they simply shared a round. Each
     * insert therefore re-reads the maximum and retries on the
     * {@code uk_fantasyRound_season_round} conflict.
     */
    private List<FantasyRound> mintRounds(String season, LocalDateTime from, int count) {
        List<MatchdayCalendar.Window> windows = MatchdayCalendar.schedule(from, count);

        List<FantasyRound> minted = new ArrayList<>(windows.size());
        int nextNumber = fantasyRoundDAO.getMaxRoundNumber(season) + 1;

        for (MatchdayCalendar.Window window : windows) {
            FantasyRound created = null;
            ConflictException lastConflict = null;

            for (int attempt = 0; attempt < ROUND_NUMBER_ATTEMPTS && created == null; attempt++) {
                try {
                    created = fantasyRoundDAO.createRound(FantasyRound.builder()
                            .roundId(UUID.randomUUID())
                            .season(season)
                            .roundNumber(nextNumber)
                            .openDate(window.openDate())
                            .lockDeadline(window.lockDeadline())
                            .endDate(window.endDate())
                            .status(FantasyRoundStatus.UPCOMING)
                            .build());
                } catch (ConflictException e) {
                    lastConflict = e;
                    nextNumber = fantasyRoundDAO.getMaxRoundNumber(season) + 1;
                    LOG.log(Level.INFO,
                            "Round number for season {0} was taken; retrying at {1}",
                            new Object[]{season, nextNumber});
                }
            }

            if (created == null) {
                throw lastConflict != null ? lastConflict
                        : new ConflictException("Unable to reserve a fantasy round number for " + season + ".");
            }

            minted.add(created);
            nextNumber++;
        }

        return minted;
    }

    /**
     * The day a round is played on. A round's lock deadline is its kickoff, so
     * this is the single source of a fixture's date -- used both when a fixture
     * is created and when a round is moved, so the two can never disagree.
     */
    private LocalDate matchDayOf(FantasyRound round) {
        return round.getLockDeadline().toLocalDate();
    }

    /**
     * The anchor for a tournament's next matchday: the moment its latest
     * scheduled round closes, so each new matchday lands strictly after the
     * previous one.
     *
     * <p>Taken from the rounds rather than from {@code now} because processing
     * usually happens on the matchday itself -- a round locking at 15:00 and
     * being simulated at 16:00 would otherwise anchor the next matchday on the
     * same day. {@code now} is still the floor, so a tournament left dormant
     * for a month does not schedule its next matchday into the past.
     */
    private LocalDateTime nextMatchdayAnchor(Tournament tournament) {
        Set<UUID> roundIds = new HashSet<>();
        for (Fixture fixture : fixtureDAO.findByTournamentId(tournament.getTournamentId())) {
            if (fixture.getRoundId() != null) {
                roundIds.add(fixture.getRoundId());
            }
        }

        LocalDateTime latest = null;
        for (UUID roundId : roundIds) {
            Optional<FantasyRound> round = fantasyRoundDAO.getRoundById(roundId);
            if (round.isEmpty()) {
                continue;
            }
            LocalDateTime closes = round.get().getEndDate() != null
                    ? round.get().getEndDate()
                    : round.get().getLockDeadline();
            latest = latest(latest, closes);
        }

        return latest(LocalDateTime.now(), latest);
    }

    /** The later of two instants, either of which may be null. */
    private LocalDateTime latest(LocalDateTime a, LocalDateTime b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isAfter(b) ? a : b;
    }

    private Fixture createFixture(UUID leagueId,
                                  Tournament tournament,
                                  FantasyRound round,
                                  RoundRobinScheduler.Pairing<UUID> pairing,
                                  TournamentStage stage,
                                  UUID poolId,
                                  Integer bracketSlot,
                                  int matchdayNumber) {
        return fixtureDAO.create(Fixture.builder()
                .fixtureId(UUID.randomUUID())
                .leagueId(leagueId)
                .roundId(round.getRoundId())
                .teamAId(pairing.homeTeamId())
                .teamBId(pairing.awayTeamId())
                // The round's lock deadline is the kickoff, so it -- not the
                // opening of the transfer window -- is the fixture's date.
                .fixtureDate(matchDayOf(round))
                .fixtureTime(MatchdayCalendar.KICKOFF)
                .status(FixtureStatus.UPCOMING)
                .tournamentId(tournament.getTournamentId())
                .poolId(poolId)
                .stage(stage)
                .bracketSlot(bracketSlot)
                .matchdayNumber(matchdayNumber)
                .build());
    }

    // ------------------------------------------------------------------
    // Progression
    // ------------------------------------------------------------------

    @Override
    public int advanceActiveTournaments() {
        List<Tournament> running = new ArrayList<>();
        running.addAll(tournamentDAO.findByStatus(TournamentStatus.POOL_STAGE));
        running.addAll(tournamentDAO.findByStatus(TournamentStatus.KNOCKOUT_STAGE));

        int advanced = 0;
        for (Tournament tournament : running) {
            try {
                if (advanceTournament(tournament.getTournamentId())) {
                    advanced++;
                }
            } catch (Exception e) {
                // One broken tournament must not stall the rest of the round.
                LOG.log(Level.SEVERE,
                        "Unable to advance tournament " + tournament.getTournamentId(), e);
            }
        }
        return advanced;
    }

    @Override
    public boolean advanceTournament(UUID tournamentId) {
        Tournament tournament = tournamentDAO.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));

        if (tournament.getStatus() == TournamentStatus.COMPLETED
                || tournament.getStatus() == TournamentStatus.CANCELLED) {
            return false;
        }

        boolean changed = refreshStandings(tournament);

        if (tournament.getStatus() == TournamentStatus.POOL_STAGE) {
            List<Fixture> poolFixtures = poolFixtures(tournament);
            if (!poolFixtures.isEmpty() && allDecided(poolFixtures)) {
                int matchdaysGenerated = matchdaysGenerated(poolFixtures);

                // Both conditions matter. "Every pool fixture is decided" is
                // true after matchday one as well, because matchday two has not
                // been written yet -- promoting to the knockout on that alone
                // would end the pool stage after a single round.
                if (matchdaysGenerated < tournament.getPoolMatchdays()) {
                    generatePoolMatchday(tournament, matchdaysGenerated + 1,
                            nextMatchdayAnchor(tournament));
                } else {
                    startKnockoutStage(tournament);
                }
                changed = true;
            }
        } else {
            changed |= advanceKnockoutStage(tournament);
        }

        return changed;
    }

    /** Recomputes every pool table from the fixtures decided so far. */
    private boolean refreshStandings(Tournament tournament) {
        PoolStandingsCalculator.Rules rules = toRules(loadSettings());
        boolean changed = false;

        for (TournamentPool pool : tournamentPoolDAO.findByTournament(tournament.getTournamentId())) {
            Map<UUID, Integer> seeds = new LinkedHashMap<>();
            for (TournamentPoolMember member : tournamentPoolDAO.findMembersByPool(pool.getPoolId())) {
                seeds.put(member.getTeamId(), member.getSeed());
            }

            List<PoolStandingsCalculator.Outcome<UUID>> outcomes = new ArrayList<>();
            for (Fixture fixture : fixtureDAO.findByPoolId(pool.getPoolId())) {
                decidedResult(fixture).ifPresent(result -> {
                    outcomes.add(new PoolStandingsCalculator.Outcome<>(
                            fixture.getTeamAId(), fixture.getTeamBId(),
                            result.getTeamAScore(), result.getTeamBScore()));
                    outcomes.add(new PoolStandingsCalculator.Outcome<>(
                            fixture.getTeamBId(), fixture.getTeamAId(),
                            result.getTeamBScore(), result.getTeamAScore()));
                });
            }

            List<PoolStandingsCalculator.Standing<UUID>> table =
                    PoolStandingsCalculator.calculate(seeds, outcomes, rules);

            for (PoolStandingsCalculator.Standing<UUID> row : table) {
                Optional<TournamentStanding> existing =
                        tournamentStandingDAO.findByPoolAndTeam(pool.getPoolId(), row.getTeamId());
                if (existing.isEmpty()) {
                    continue;
                }

                TournamentStanding standing = existing.get();
                standing.setPlayed(row.getPlayed());
                standing.setWon(row.getWon());
                standing.setDrawn(row.getDrawn());
                standing.setLost(row.getLost());
                standing.setPointsFor(row.getPointsFor());
                standing.setPointsAgainst(row.getPointsAgainst());
                standing.setAttackBonus(row.getAttackBonus());
                standing.setLosingBonus(row.getLosingBonus());
                standing.setTournamentPoints(row.getTournamentPoints());
                standing.setPosition(row.getPosition());

                tournamentStandingDAO.update(standing);
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Turns finished pools into a knockout bracket: pool winners first, then
     * the best runners-up, exactly as the Rugby World Cup fills its
     * quarter-finals.
     */
    private void startKnockoutStage(Tournament tournament) {
        // Loaded only to fail fast on a league that has gone missing; the
        // fixtures themselves need nothing but its id.
        League league = leagueDAO.findLeagueById(tournament.getLeagueId())
                .orElseThrow(() -> new ResourceNotFoundException("League not found"));

        List<TournamentStanding> standings =
                tournamentStandingDAO.findByTournament(tournament.getTournamentId());

        // Rank by finishing position first so every pool winner outranks every
        // runner-up, then separate managers on equal position by their record.
        standings.sort(Comparator
                .comparingInt((TournamentStanding standing) ->
                        standing.getPosition() == null ? Integer.MAX_VALUE : standing.getPosition())
                .thenComparing(Comparator.comparingInt(TournamentStanding::getTournamentPoints).reversed())
                .thenComparing(Comparator.comparingInt(TournamentStanding::getPointsDifference).reversed())
                .thenComparing(Comparator.comparingInt(TournamentStanding::getPointsFor).reversed())
                .thenComparing(standing -> standing.getTeamId().toString()));

        List<UUID> qualifiers = new ArrayList<>();
        for (TournamentStanding standing : standings) {
            if (qualifiers.size() >= tournament.getBracketSize()) {
                break;
            }
            qualifiers.add(standing.getTeamId());

            standing.setQualified(true);
            tournamentStandingDAO.update(standing);
        }

        TournamentStage stage = TournamentStage.forTeamsRemaining(tournament.getBracketSize());
        List<RoundRobinScheduler.Pairing<UUID>> pairings = KnockoutBracket.firstRound(qualifiers);

        int matchday = tournament.getPoolMatchdays() + 1;
        FantasyRound round = mintRounds(tournament.getSeason(), nextMatchdayAnchor(tournament), 1).get(0);

        for (int slot = 0; slot < pairings.size(); slot++) {
            createFixture(league.getLeagueId(), tournament, round, pairings.get(slot),
                    stage, null, slot, matchday);
        }

        tournamentDAO.updateStatus(tournament.getTournamentId(), TournamentStatus.KNOCKOUT_STAGE);
    }

    /**
     * Plays the bracket forward one round at a time. A beaten manager is out;
     * only the losing semi-finalists get another fixture, and only when the
     * third-place playoff is enabled.
     */
    private boolean advanceKnockoutStage(Tournament tournament) {
        List<Fixture> knockoutFixtures = new ArrayList<>();
        for (Fixture fixture : fixtureDAO.findByTournamentId(tournament.getTournamentId())) {
            if (fixture.getStage() != null && fixture.getStage().isKnockout()) {
                knockoutFixtures.add(fixture);
            }
        }
        if (knockoutFixtures.isEmpty()) {
            return false;
        }

        Map<TournamentStage, List<Fixture>> byStage = new EnumMap<>(TournamentStage.class);
        for (Fixture fixture : knockoutFixtures) {
            byStage.computeIfAbsent(fixture.getStage(), key -> new ArrayList<>()).add(fixture);
        }

        // The final round is the end of the tournament rather than a stage to
        // build on, so it is handled separately from the rounds that feed it.
        if (byStage.containsKey(TournamentStage.FINAL)) {
            return crownChampion(tournament, byStage);
        }

        TournamentStage current = byStage.keySet().stream()
                .max(Comparator.comparingInt(Enum::ordinal))
                .orElseThrow();

        List<Fixture> currentFixtures = new ArrayList<>(byStage.get(current));
        currentFixtures.sort(Comparator.comparingInt(fixture ->
                fixture.getBracketSlot() == null ? 0 : fixture.getBracketSlot()));

        if (!allDecided(currentFixtures)) {
            return false;
        }

        Map<UUID, Integer> seeds = seedsFor(tournament);
        List<UUID> winners = new ArrayList<>();
        List<UUID> losers = new ArrayList<>();
        for (Fixture fixture : currentFixtures) {
            MatchResult result = decidedResult(fixture).orElseThrow();
            UUID winner = winnerOf(fixture, result, seeds);
            winners.add(winner);
            losers.add(winner.equals(fixture.getTeamAId()) ? fixture.getTeamBId() : fixture.getTeamAId());
        }

        League league = leagueDAO.findLeagueById(tournament.getLeagueId())
                .orElseThrow(() -> new ResourceNotFoundException("League not found"));

        int matchday = (currentFixtures.get(0).getMatchdayNumber() == null
                ? tournament.getPoolMatchdays() + 1
                : currentFixtures.get(0).getMatchdayNumber()) + 1;
        // Anchored off the last played round's end, not "now", so each knockout
        // matchday lands on the next valid match day strictly after the previous one.
        FantasyRound round = mintRounds(tournament.getSeason(), nextMatchdayAnchor(tournament), 1).get(0);

        TournamentStage nextStage = TournamentStage.forTeamsRemaining(winners.size());
        List<RoundRobinScheduler.Pairing<UUID>> nextPairings = KnockoutBracket.nextRound(winners);
        for (int slot = 0; slot < nextPairings.size(); slot++) {
            createFixture(league.getLeagueId(), tournament, round, nextPairings.get(slot), nextStage, null, slot, matchday);
        }

        // The two beaten semi-finalists meet on the same matchday as the final.
        if (nextStage == TournamentStage.FINAL && tournament.isThirdPlacePlayoff() && losers.size() == 2) {
            createFixture(league.getLeagueId(), tournament, round,
                    new RoundRobinScheduler.Pairing<>(losers.get(0), losers.get(1)),
                    TournamentStage.THIRD_PLACE, null, 0, matchday);
        }

        return true;
    }

    private boolean crownChampion(Tournament tournament, Map<TournamentStage, List<Fixture>> byStage) {
        Fixture finalFixture = byStage.get(TournamentStage.FINAL).get(0);
        Optional<MatchResult> finalResult = decidedResult(finalFixture);
        if (finalResult.isEmpty()) {
            return false;
        }

        List<Fixture> thirdPlaceFixtures = byStage.get(TournamentStage.THIRD_PLACE);
        Optional<MatchResult> thirdPlaceResult = Optional.empty();
        if (thirdPlaceFixtures != null && !thirdPlaceFixtures.isEmpty()) {
            thirdPlaceResult = decidedResult(thirdPlaceFixtures.get(0));
            if (thirdPlaceResult.isEmpty()) {
                // Wait for the playoff so the podium is recorded in one go.
                return false;
            }
        }

        Map<UUID, Integer> seeds = seedsFor(tournament);

        UUID champion = winnerOf(finalFixture, finalResult.get(), seeds);
        UUID runnerUp = champion.equals(finalFixture.getTeamAId())
                ? finalFixture.getTeamBId()
                : finalFixture.getTeamAId();

        UUID thirdPlace = null;
        if (thirdPlaceResult.isPresent()) {
            thirdPlace = winnerOf(thirdPlaceFixtures.get(0), thirdPlaceResult.get(), seeds);
        }

        tournamentDAO.complete(tournament.getTournamentId(), champion, runnerUp, thirdPlace);

        League league = leagueDAO.findLeagueById(tournament.getLeagueId()).orElse(null);
        if (league != null) {
            leagueDAO.updateStatus(league.getLeagueId(), LeagueStatus.COMPLETED,
                    league.getStartedAt() != null ? league.getStartedAt() : LocalDateTime.now());
        }

        return true;
    }

    /**
     * A knockout fixture has to produce someone. Fantasy scores can finish
     * level, in which case the better pool seed goes through rather than the
     * tie standing forever.
     */
    private UUID winnerOf(Fixture fixture, MatchResult result, Map<UUID, Integer> seeds) {
        if (result.getTeamAScore() > result.getTeamBScore()) {
            return fixture.getTeamAId();
        }
        if (result.getTeamBScore() > result.getTeamAScore()) {
            return fixture.getTeamBId();
        }

        int seedA = seeds.getOrDefault(fixture.getTeamAId(), Integer.MAX_VALUE);
        int seedB = seeds.getOrDefault(fixture.getTeamBId(), Integer.MAX_VALUE);
        return seedA <= seedB ? fixture.getTeamAId() : fixture.getTeamBId();
    }

    private Map<UUID, Integer> seedsFor(Tournament tournament) {
        Map<UUID, Integer> seeds = new HashMap<>();
        for (TournamentPoolMember member : tournamentPoolDAO.findMembersByTournament(tournament.getTournamentId())) {
            seeds.put(member.getTeamId(), member.getSeed());
        }
        return seeds;
    }

    private List<Fixture> poolFixtures(Tournament tournament) {
        List<Fixture> fixtures = new ArrayList<>();
        for (Fixture fixture : fixtureDAO.findByTournamentId(tournament.getTournamentId())) {
            if (fixture.getStage() == TournamentStage.POOL) {
                fixtures.add(fixture);
            }
        }
        return fixtures;
    }

    /**
     * How many pool matchdays have actually been written. Compared against
     * {@code tournament.poolMatchdays} to tell "the pool stage is over" apart
     * from "the matchdays scheduled so far are over".
     */
    private int matchdaysGenerated(List<Fixture> fixtures) {
        int highest = 0;
        for (Fixture fixture : fixtures) {
            if (fixture.getMatchdayNumber() != null && fixture.getMatchdayNumber() > highest) {
                highest = fixture.getMatchdayNumber();
            }
        }
        return highest;
    }

    private boolean allDecided(List<Fixture> fixtures) {
        for (Fixture fixture : fixtures) {
            if (fixture.getStatus() == FixtureStatus.CANCELLED) {
                continue;
            }
            if (decidedResult(fixture).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** A fixture counts as decided once it has been simulated and holds a current result. */
    private Optional<MatchResult> decidedResult(Fixture fixture) {
        if (fixture.getStatus() != FixtureStatus.COMPLETED
                && fixture.getStatus() != FixtureStatus.PROCESSED) {
            return Optional.empty();
        }
        return matchResultDAO.findCurrentByFixtureId(fixture.getFixtureId());
    }

    // ------------------------------------------------------------------
    // Queries
    // ------------------------------------------------------------------

    @Override
    public TournamentResponseDTO getTournamentForLeague(UUID leagueId) {
        if (leagueId == null) {
            throw new ValidationException("A league is required.");
        }

        Tournament tournament = tournamentDAO.findActiveByLeague(leagueId)
                .or(() -> tournamentDAO.findByLeague(leagueId).stream()
                        .max(Comparator.comparing(Tournament::getCreatedAt,
                                Comparator.nullsFirst(Comparator.naturalOrder()))))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "This league has not started a tournament yet."));

        return mapTournament(tournament);
    }

    @Override
    public TournamentResponseDTO getTournament(UUID tournamentId) {
        if (tournamentId == null) {
            throw new ValidationException("A tournament is required.");
        }
        Tournament tournament = tournamentDAO.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));
        return mapTournament(tournament);
    }

    @Override
    public List<TournamentFixtureResponseDTO> getTournamentFixtures(UUID tournamentId) {
        if (tournamentId == null) {
            throw new ValidationException("A tournament is required.");
        }
        Tournament tournament = tournamentDAO.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));

        Map<UUID, String> poolNames = new HashMap<>();
        for (TournamentPool pool : tournamentPoolDAO.findByTournament(tournamentId)) {
            poolNames.put(pool.getPoolId(), pool.getPoolName());
        }

        List<TournamentFixtureResponseDTO> responses = new ArrayList<>();
        for (Fixture fixture : fixtureDAO.findByTournamentId(tournament.getTournamentId())) {
            Optional<MatchResult> result = matchResultDAO.findCurrentByFixtureId(fixture.getFixtureId());
            Integer roundNumber = fantasyRoundDAO.getRoundById(fixture.getRoundId())
                    .map(FantasyRound::getRoundNumber)
                    .orElse(null);

            responses.add(TournamentFixtureResponseDTO.builder()
                    .fixtureId(fixture.getFixtureId())
                    .tournamentId(fixture.getTournamentId())
                    .stage(fixture.getStage())
                    // Jackson writes the enum by name(), so the label has to
                    // travel as its own field or the frontend is left
                    // reinventing it (as it used to).
                    .stageLabel(stageLabel(fixture.getStage()))
                    .poolId(fixture.getPoolId())
                    .poolName(poolNames.get(fixture.getPoolId()))
                    .bracketSlot(fixture.getBracketSlot())
                    .matchdayNumber(fixture.getMatchdayNumber())
                    .roundId(fixture.getRoundId())
                    .roundNumber(roundNumber)
                    .teamAId(fixture.getTeamAId())
                    .teamAName(teamName(fixture.getTeamAId()))
                    .teamBId(fixture.getTeamBId())
                    .teamBName(teamName(fixture.getTeamBId()))
                    .teamAScore(result.map(MatchResult::getTeamAScore).orElse(null))
                    .teamBScore(result.map(MatchResult::getTeamBScore).orElse(null))
                    .status(fixture.getStatus())
                    .fixtureDate(fixture.getFixtureDate())
                    .fixtureTime(fixture.getFixtureTime())
                    .build());
        }
        return responses;
    }

    private TournamentResponseDTO mapTournament(Tournament tournament) {
        List<TournamentPoolResponseDTO> pools = new ArrayList<>();

        for (TournamentPool pool : tournamentPoolDAO.findByTournament(tournament.getTournamentId())) {
            Map<UUID, Integer> seeds = new HashMap<>();
            for (TournamentPoolMember member : tournamentPoolDAO.findMembersByPool(pool.getPoolId())) {
                seeds.put(member.getTeamId(), member.getSeed());
            }

            List<TournamentStanding> standings = tournamentStandingDAO.findByPool(pool.getPoolId());
            standings.sort(Comparator.comparingInt(standing ->
                    standing.getPosition() == null ? Integer.MAX_VALUE : standing.getPosition()));

            List<TournamentStandingResponseDTO> rows = new ArrayList<>();
            for (TournamentStanding standing : standings) {
                rows.add(TournamentStandingResponseDTO.builder()
                        .teamId(standing.getTeamId())
                        .teamName(teamName(standing.getTeamId()))
                        .ownerUsername(ownerUsername(standing.getTeamId()))
                        .seed(seeds.getOrDefault(standing.getTeamId(), 0))
                        .played(standing.getPlayed())
                        .won(standing.getWon())
                        .drawn(standing.getDrawn())
                        .lost(standing.getLost())
                        .pointsFor(standing.getPointsFor())
                        .pointsAgainst(standing.getPointsAgainst())
                        .pointsDifference(standing.getPointsDifference())
                        .attackBonus(standing.getAttackBonus())
                        .losingBonus(standing.getLosingBonus())
                        .tournamentPoints(standing.getTournamentPoints())
                        .position(standing.getPosition())
                        .qualified(standing.isQualified())
                        .build());
            }

            pools.add(TournamentPoolResponseDTO.builder()
                    .poolId(pool.getPoolId())
                    .poolName(pool.getPoolName())
                    .poolSize(pool.getPoolSize())
                    .standings(rows)
                    .build());
        }

        String leagueName = leagueDAO.findLeagueById(tournament.getLeagueId())
                .map(League::getLeagueName)
                .orElse(null);

        return TournamentResponseDTO.builder()
                .tournamentId(tournament.getTournamentId())
                .leagueId(tournament.getLeagueId())
                .leagueName(leagueName)
                .season(tournament.getSeason())
                .status(tournament.getStatus())
                .managerCount(tournament.getManagerCount())
                .poolCount(tournament.getPoolCount())
                .poolMatchdays(tournament.getPoolMatchdays())
                .bracketSize(tournament.getBracketSize())
                .thirdPlacePlayoff(tournament.isThirdPlacePlayoff())
                .championTeamId(tournament.getChampionTeamId())
                .championTeamName(teamName(tournament.getChampionTeamId()))
                .runnerUpTeamId(tournament.getRunnerUpTeamId())
                .runnerUpTeamName(teamName(tournament.getRunnerUpTeamId()))
                .thirdPlaceTeamId(tournament.getThirdPlaceTeamId())
                .thirdPlaceTeamName(teamName(tournament.getThirdPlaceTeamId()))
                .createdAt(tournament.getCreatedAt())
                .completedAt(tournament.getCompletedAt())
                .pools(pools)
                .build();
    }

    private String teamName(UUID teamId) {
        if (teamId == null) {
            return null;
        }
        return fantasyTeamDAO.getTeamById(teamId).map(FantasyTeam::getTeamName).orElse(null);
    }

    private String ownerUsername(UUID teamId) {
        if (teamId == null) {
            return null;
        }
        return fantasyTeamDAO.getTeamById(teamId)
                .map(FantasyTeam::getOwnerUserId)
                .flatMap(userDAO::getUserById)
                .map(User::getUsername)
                .orElse(null);
    }

    // ------------------------------------------------------------------
    // Rescheduling
    // ------------------------------------------------------------------

    @Override
    public MatchDayResponseDTO updateMatchDay(UUID actorUserId, UUID leagueId, UUID roundId, LocalDate matchDay) {
        // 1. Nothing may be missing. Checked before anything is read so a
        //    malformed request never reaches the database.
        if (actorUserId == null || leagueId == null || roundId == null || matchDay == null) {
            throw new ValidationException("A league, a round and a match day are all required.");
        }

        // 2. Both must exist.
        League league = leagueDAO.findLeagueById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found"));
        FantasyRound round = fantasyRoundDAO.getRoundById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy round not found"));

        // 3. Who may move it.
        requireMatchDayEditor(actorUserId, league);

        // 4. The round has to be this league's round. A fantasyRound row is
        //    global, so without this a manager could pass any roundId and move
        //    someone else's competition -- including the archived 2025 leagues,
        //    whose hand-written finals are meant to stay exactly where they are.
        //    Rounds are minted per league (see mintRounds), so "every fixture in
        //    this round belongs to this league" is the honest expression of
        //    ownership; do not weaken it to "at least one".
        List<Fixture> fixtures = fixtureDAO.findByRoundId(roundId);
        if (fixtures.isEmpty()) {
            throw new BusinessRuleException("This round has no fixtures, so there is nothing to move.");
        }
        for (Fixture fixture : fixtures) {
            if (!leagueId.equals(fixture.getLeagueId())) {
                throw new BusinessRuleException(
                        "This round is shared with another league and cannot be rescheduled from here.");
            }
        }

        // 5. Only a round that has not opened yet.
        if (round.getStatus() != FantasyRoundStatus.UPCOMING) {
            throw new BusinessRuleException("This round has already opened and cannot be moved.");
        }

        // 6. And nothing in it may have been played. Belt and braces with (5):
        //    the round status is the gate, a played fixture is the evidence.
        for (Fixture fixture : fixtures) {
            if (fixture.getStatus() != FixtureStatus.UPCOMING
                    || matchResultDAO.findCurrentByFixtureId(fixture.getFixtureId()).isPresent()) {
                throw new BusinessRuleException(
                        "A fixture in this round has already been played, so the round cannot be moved.");
            }
        }

        // 7. The same matchday predicate the generator uses, so a date one
        //    accepts the other can never reject.
        if (!MatchdayCalendar.isMatchDay(matchDay)) {
            throw new ValidationException("Match days must fall on a Wednesday, Saturday or Sunday.");
        }

        // 8. Still in the future.
        LocalDateTime kickoff = matchDay.atTime(MatchdayCalendar.KICKOFF);
        if (!MatchDayEditRules.isInFuture(matchDay, LocalDateTime.now())) {
            throw new ValidationException("A match day must be in the future.");
        }

        // 9. And it must not reorder the competition.
        UUID tournamentId = tournamentIdOf(fixtures);
        FantasyRound previous = neighbourRound(tournamentId, round, true);
        FantasyRound next = neighbourRound(tournamentId, round, false);
        LocalDateTime previousEnd = previous == null ? null : roundCloses(previous);
        LocalDateTime nextOpen = next == null ? null : next.getOpenDate();

        if (!MatchDayEditRules.fitsBetween(kickoff, previousEnd, nextOpen)) {
            throw new BusinessRuleException(
                    "A match day has to stay between the matchday before it and the matchday after it, "
                            + "so the tournament is played in the order it was drawn.");
        }

        // One window, three timestamps, one statement: chk_fantasyRound_dates
        // spans all three, so a column-by-column update would be rejected on an
        // intermediate state. The transfer window opens where the previous
        // matchday closed, exactly as MatchdayCalendar.schedule tiles them.
        LocalDateTime openDate = previousEnd != null ? previousEnd.plusSeconds(1) : round.getOpenDate();
        MatchdayCalendar.Window window = MatchdayCalendar.windowFor(matchDay, openDate);

        if (!fantasyRoundDAO.updateRoundSchedule(roundId,
                window.openDate(), window.lockDeadline(), window.endDate())) {
            // The DAO's own WHERE carries the UPCOMING guard, so this is the
            // round having opened between check (5) and the write.
            throw new BusinessRuleException("This round has already opened and cannot be moved.");
        }

        round.setOpenDate(window.openDate());
        round.setLockDeadline(window.lockDeadline());
        round.setEndDate(window.endDate());

        int fixturesMoved = moveFixturesTo(fixtures, matchDayOf(round));

        Fixture sample = fixtures.get(0);
        return MatchDayResponseDTO.builder()
                .roundId(roundId)
                .matchdayNumber(sample.getMatchdayNumber())
                .stage(sample.getStage())
                .stageLabel(stageLabel(sample.getStage()))
                .matchDay(matchDayOf(round))
                .kickoff(MatchdayCalendar.KICKOFF)
                .fixturesMoved(fixturesMoved)
                .build();
    }

    @Override
    public MatchDayResponseDTO playRoundNow(UUID actorUserId, UUID roundId) {
        requireAdmin(actorUserId);

        if (roundId == null) {
            throw new ValidationException("A round is required.");
        }

        FantasyRound round = fantasyRoundDAO.getRoundById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy round not found"));

        if (round.getStatus() != FantasyRoundStatus.UPCOMING
                && round.getStatus() != FantasyRoundStatus.OPEN) {
            throw new BusinessRuleException(
                    "Only a round that has not locked yet can be played early; this one is "
                            + round.getStatus() + ".");
        }

        // Pull the whole window back to the present. Not built from
        // MatchdayCalendar.windowFor, which pins the lock deadline to the 15:00
        // kickoff -- an override run at 10:00 would then set a deadline five
        // hours away and nothing would happen. Built as one Window all the same,
        // so the three timestamps are still written together and
        // chk_fantasyRound_dates (lockDeadline >= openDate, endDate >=
        // lockDeadline) holds by construction.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime openDate = now.minusMinutes(1);
        LocalDateTime lockDeadline = now.minusSeconds(1);
        LocalDateTime endDate = latest(now.toLocalDate().atTime(23, 59, 59), lockDeadline);
        MatchdayCalendar.Window window =
                new MatchdayCalendar.Window(now.toLocalDate(), openDate, lockDeadline, endDate);

        // forceRoundSchedule, not updateRoundSchedule: the latter's WHERE is
        // pinned to status = 'UPCOMING' and would silently no-op on the OPEN
        // round this override exists to reach.
        if (!fantasyRoundDAO.forceRoundSchedule(roundId,
                window.openDate(), window.lockDeadline(), window.endDate())) {
            throw new BusinessRuleException("This round has already locked and cannot be played early.");
        }

        round.setOpenDate(window.openDate());
        round.setLockDeadline(window.lockDeadline());
        round.setEndDate(window.endDate());

        // Keep the invariant the whole calendar rests on: a fixture's date is
        // its round's lock deadline. Leaving the fixtures dated next Saturday
        // while the round is being played today would put the two out of step.
        List<Fixture> fixtures = fixtureDAO.findByRoundId(roundId);
        int fixturesMoved = moveFixturesTo(fixtures, matchDayOf(round), window.lockDeadline().toLocalTime());

        Fixture sample = fixtures.isEmpty() ? null : fixtures.get(0);
        return MatchDayResponseDTO.builder()
                .roundId(roundId)
                .matchdayNumber(sample == null ? null : sample.getMatchdayNumber())
                .stage(sample == null ? null : sample.getStage())
                .stageLabel(sample == null ? null : stageLabel(sample.getStage()))
                .matchDay(matchDayOf(round))
                .kickoff(window.lockDeadline().toLocalTime())
                .fixturesMoved(fixturesMoved)
                .build();
    }

    /** Re-dates every fixture of a round, leaving status and simulationDate alone. */
    private int moveFixturesTo(List<Fixture> fixtures, LocalDate matchDay) {
        return moveFixturesTo(fixtures, matchDay, MatchdayCalendar.KICKOFF);
    }

    private int moveFixturesTo(List<Fixture> fixtures, LocalDate matchDay, LocalTime kickoff) {
        int moved = 0;
        for (Fixture fixture : fixtures) {
            // Reloaded so the write carries the fixture's current status and
            // simulationDate rather than a stale copy -- updateFixture writes
            // all four columns, so only date and time must actually change.
            Optional<Fixture> stored = fixtureDAO.findById(fixture.getFixtureId());
            if (stored.isEmpty()) {
                continue;
            }
            Fixture toMove = stored.get();
            toMove.setFixtureDate(matchDay);
            toMove.setFixtureTime(kickoff);
            if (fixtureDAO.updateFixture(toMove)) {
                moved++;
            }
        }
        return moved;
    }

    /** The tournament a round's fixtures belong to, or null for a non-tournament round. */
    private UUID tournamentIdOf(List<Fixture> fixtures) {
        for (Fixture fixture : fixtures) {
            if (fixture.getTournamentId() != null) {
                return fixture.getTournamentId();
            }
        }
        return null;
    }

    /**
     * The round played immediately before (or after) {@code round} within the
     * same tournament, ordered by lock deadline -- the kickoff, and so the only
     * ordering a manager can see. Null when there is nothing on that side, or
     * when the round belongs to no tournament at all.
     */
    private FantasyRound neighbourRound(UUID tournamentId, FantasyRound round, boolean before) {
        if (tournamentId == null) {
            return null;
        }

        Set<UUID> roundIds = new HashSet<>();
        for (Fixture fixture : fixtureDAO.findByTournamentId(tournamentId)) {
            if (fixture.getRoundId() != null && !fixture.getRoundId().equals(round.getRoundId())) {
                roundIds.add(fixture.getRoundId());
            }
        }

        FantasyRound best = null;
        for (UUID roundId : roundIds) {
            Optional<FantasyRound> candidate = fantasyRoundDAO.getRoundById(roundId);
            if (candidate.isEmpty() || candidate.get().getLockDeadline() == null) {
                continue;
            }
            LocalDateTime deadline = candidate.get().getLockDeadline();

            if (before) {
                if (deadline.isBefore(round.getLockDeadline())
                        && (best == null || deadline.isAfter(best.getLockDeadline()))) {
                    best = candidate.get();
                }
            } else {
                if (deadline.isAfter(round.getLockDeadline())
                        && (best == null || deadline.isBefore(best.getLockDeadline()))) {
                    best = candidate.get();
                }
            }
        }
        return best;
    }

    /** When a round's window closes; endDate, falling back to the lock deadline. */
    private LocalDateTime roundCloses(FantasyRound round) {
        return round.getEndDate() != null ? round.getEndDate() : round.getLockDeadline();
    }

    /** The manager-facing name of a stage; null-safe, and the one place it is read. */
    private String stageLabel(TournamentStage stage) {
        return stage == null ? null : stage.getLabel();
    }

    // ------------------------------------------------------------------
    // Settings
    // ------------------------------------------------------------------

    @Override
    public TournamentSettingsDTO getSettings() {
        return mapSettings(loadSettings());
    }

    @Override
    public TournamentSettingsDTO updateSettings(UUID actorUserId, TournamentSettingsDTO request) {
        requireAdmin(actorUserId);

        if (request == null) {
            throw new ValidationException("Tournament settings are required.");
        }
        if (request.getWinPoints() < request.getDrawPoints()
                || request.getDrawPoints() < request.getLossPoints()
                || request.getLossPoints() < 0) {
            throw new ValidationException(
                    "Win points must not be below draw points, which must not be below loss points.");
        }
        if (request.getAttackBonusThreshold() <= 0) {
            throw new ValidationException("The attacking bonus threshold must be greater than zero.");
        }
        if (request.getLosingBonusMargin() < 0) {
            throw new ValidationException("The losing bonus margin cannot be negative.");
        }

        TournamentSettings settings = loadSettings();
        settings.setWinPoints(request.getWinPoints());
        settings.setDrawPoints(request.getDrawPoints());
        settings.setLossPoints(request.getLossPoints());
        settings.setAttackBonusThreshold(request.getAttackBonusThreshold());
        settings.setLosingBonusMargin(request.getLosingBonusMargin());
        settings.setThirdPlacePlayoff(request.isThirdPlacePlayoff());

        tournamentSettingsDAO.updateSettings(settings);
        return mapSettings(loadSettings());
    }

    /**
     * Reads the single settings row, recreating it from the Rugby World Cup
     * defaults if it has gone missing rather than leaving tournaments
     * ungeneratable.
     */
    private TournamentSettings loadSettings() {
        return tournamentSettingsDAO.getSettings().orElseGet(() -> {
            LOG.warning("Tournament settings row was missing; recreating it from defaults.");
            PoolStandingsCalculator.Rules defaults = PoolStandingsCalculator.Rules.rugbyWorldCupDefaults();
            return tournamentSettingsDAO.insertSettings(TournamentSettings.builder()
                    .settingsId(UUID.randomUUID())
                    .winPoints(defaults.winPoints())
                    .drawPoints(defaults.drawPoints())
                    .lossPoints(defaults.lossPoints())
                    .attackBonusThreshold(defaults.attackBonusThreshold())
                    .losingBonusMargin(defaults.losingBonusMargin())
                    .thirdPlacePlayoff(true)
                    .build());
        });
    }

    private PoolStandingsCalculator.Rules toRules(TournamentSettings settings) {
        return new PoolStandingsCalculator.Rules(
                settings.getWinPoints(),
                settings.getDrawPoints(),
                settings.getLossPoints(),
                settings.getAttackBonusThreshold(),
                settings.getLosingBonusMargin());
    }

    private TournamentSettingsDTO mapSettings(TournamentSettings settings) {
        return TournamentSettingsDTO.builder()
                .settingsId(settings.getSettingsId())
                .winPoints(settings.getWinPoints())
                .drawPoints(settings.getDrawPoints())
                .lossPoints(settings.getLossPoints())
                .attackBonusThreshold(settings.getAttackBonusThreshold())
                .losingBonusMargin(settings.getLosingBonusMargin())
                .thirdPlacePlayoff(settings.isThirdPlacePlayoff())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    // ------------------------------------------------------------------
    // Authorisation
    // ------------------------------------------------------------------

    /**
     * Who may start a league. The league manager may always start their own league,
     * whatever its type. An administrator runs the competition proper, so they may
     * start a PUBLIC league -- but a PRIVATE league is a group of friends around
     * their own manager, and it is that manager's call when it kicks off. Admins
     * keep full VIEW access to private leagues (see LeagueVisibility.canView); only
     * starting one is withheld.
     *
     * <p>startLeague is the only caller, so this stays a single start-specific check
     * rather than a shared manager-or-admin predicate that would quietly acquire the
     * league-type rule for some other call site.
     */
    private void requireCanStartLeague(UUID actorUserId, League league) {
        if (actorUserId == null) {
            throw new ValidationException("An authenticated user is required.");
        }

        User user = userDAO.getUserById(actorUserId)
                .orElseThrow(() -> new AuthorisationException("An authenticated user is required."));

        if (actorUserId.equals(league.getManagerUserId())) {
            return;
        }
        if (user.getRole() == UserRole.ADMINISTRATOR) {
            if (league.getLeagueType() == LeagueType.PUBLIC) {
                return;
            }
            throw new AuthorisationException(
                    "Administrators can only start public leagues. A private league is started by its own manager.");
        }
        throw new AuthorisationException("Only the league manager or an administrator can start this league.");
    }

    /**
     * Who may move a league's match days. The one expression of the rule, in
     * one method, called from one place -- this codebase has broken repeatedly
     * by inlining copies of a league rule until the copies disagreed (see
     * {@link LeagueVisibility} and CLAUDE.md). {@code TournamentServlet}'s
     * {@code canEditMatchDays} mirrors it for display only; the decision is
     * here.
     *
     * <p>An administrator runs the competition and may reschedule any league. A
     * PRIVATE league is a group of friends around their own manager, so that
     * manager may move their own match days. A PUBLIC league is the competition
     * proper: its calendar is not a manager's to move, even their own.
     */
    private void requireMatchDayEditor(UUID actorUserId, League league) {
        if (actorUserId == null) {
            throw new ValidationException("An authenticated user is required.");
        }

        User user = userDAO.getUserById(actorUserId)
                .orElseThrow(() -> new AuthorisationException("An authenticated user is required."));

        if (user.getRole() == UserRole.ADMINISTRATOR) {
            return;
        }
        if (league.getLeagueType() == LeagueType.PRIVATE
                && actorUserId.equals(league.getManagerUserId())) {
            return;
        }
        if (league.getLeagueType() == LeagueType.PUBLIC) {
            throw new AuthorisationException(
                    "Only an administrator can reschedule a public league's match days.");
        }
        throw new AuthorisationException(
                "Only this league's own manager can reschedule its match days.");
    }

    private void requireAdmin(UUID actorUserId) {
        if (actorUserId == null) {
            throw new ValidationException("An authenticated administrator is required.");
        }

        User user = userDAO.getUserById(actorUserId)
                .orElseThrow(() -> new AuthorisationException("An authenticated administrator is required."));

        if (user.getRole() != UserRole.ADMINISTRATOR) {
            throw new AuthorisationException("Only admins can perform this action.");
        }
    }
}
