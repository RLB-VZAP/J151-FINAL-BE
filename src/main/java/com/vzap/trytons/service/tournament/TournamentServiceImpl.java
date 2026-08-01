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
import com.vzap.trytons.util.tournament.KnockoutBracket;
import com.vzap.trytons.util.tournament.PoolAllocator;
import com.vzap.trytons.util.tournament.PoolStandingsCalculator;
import com.vzap.trytons.util.tournament.RoundRobinScheduler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

    /** Fantasy fixtures are notional, so every generated fixture kicks off at the same time. */
    private static final LocalTime DEFAULT_KICKOFF = LocalTime.of(15, 0);
    private static final int ROUND_SPACING_DAYS = 7;

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

        requireManagerOrAdmin(actorUserId, league);

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
            fixturesGenerated = generatePoolStage(league, tournament, seededTeams, draw, season, poolMatchdays);
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
                .fixturesGenerated(fixturesGenerated)
                .message(managerCount + " managers drawn into " + poolSizes.size()
                        + " pool(s), " + fixturesGenerated + " pool fixtures generated over "
                        + poolMatchdays + " rounds, followed by a "
                        + bracketSize + "-team knockout stage.")
                .build();
    }

    /**
     * Writes the pools, their members, empty standings rows and every pool
     * fixture. Split out from {@link #startLeague} so a failure can be undone
     * as a unit.
     *
     * @return how many fixtures were generated
     */
    private int generatePoolStage(League league,
                                  Tournament tournament,
                                  List<UUID> seededTeams,
                                  List<List<UUID>> draw,
                                  String season,
                                  int poolMatchdays) {
        List<FantasyRound> rounds = allocateRounds(league.getLeagueId(), season, poolMatchdays);

        Map<UUID, Integer> seedByTeam = new HashMap<>();
        for (int i = 0; i < seededTeams.size(); i++) {
            seedByTeam.put(seededTeams.get(i), i + 1);
        }

        int fixturesGenerated = 0;
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

            List<List<RoundRobinScheduler.Pairing<UUID>>> schedule = RoundRobinScheduler.schedule(poolTeams);
            for (int matchday = 0; matchday < schedule.size(); matchday++) {
                FantasyRound round = rounds.get(matchday);
                for (RoundRobinScheduler.Pairing<UUID> pairing : schedule.get(matchday)) {
                    createFixture(league, tournament, round, pairing,
                            TournamentStage.POOL, pool.getPoolId(), null, matchday + 1);
                    fixturesGenerated++;
                }
            }
        }


        // Seed the tables so a freshly drawn pool reads as a proper standings
        // table -- everyone on nothing, ordered by draw seed -- rather than an
        // unordered list with no positions until the first result lands.
        refreshStandings(tournament);

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
     * Claims the next upcoming fantasy rounds, creating extra ones only when
     * the season does not already stretch far enough. One matchday maps onto
     * one round, so a fixture is decided by the fantasy points its two squads
     * score over that round's real rugby matches.
     *
     * <p>Rounds in which the league already holds a fixture are skipped. A
     * fantasy team may only appear once per league round -- the database
     * enforces this in {@code trg_fixture_integrity_insert} -- so a league
     * carrying pre-existing fixtures would otherwise have its generated
     * tournament rejected mid-write.
     */
    private List<FantasyRound> allocateRounds(UUID leagueId, String season, int required) {
        List<FantasyRound> seasonRounds = new ArrayList<>();
        for (FantasyRound round : fantasyRoundDAO.getAllRounds()) {
            if (season.equals(round.getSeason())) {
                seasonRounds.add(round);
            }
        }

        Set<UUID> roundsAlreadyUsedByLeague = new HashSet<>();
        for (Fixture fixture : fixtureDAO.findByLeagueId(leagueId)) {
            if (fixture.getStatus() != FixtureStatus.CANCELLED) {
                roundsAlreadyUsedByLeague.add(fixture.getRoundId());
            }
        }

        List<FantasyRound> available = new ArrayList<>();
        for (FantasyRound round : seasonRounds) {
            if (round.getStatus() == FantasyRoundStatus.UPCOMING
                    && !roundsAlreadyUsedByLeague.contains(round.getRoundId())) {
                available.add(round);
            }
        }
        available.sort(Comparator.comparingInt(FantasyRound::getRoundNumber));

        List<FantasyRound> claimed = new ArrayList<>(
                available.subList(0, Math.min(required, available.size())));

        if (claimed.size() < required) {
            int nextNumber = fantasyRoundDAO.getMaxRoundNumber(season) + 1;

            LocalDateTime cursor = seasonRounds.stream()
                    .map(round -> round.getEndDate() != null ? round.getEndDate() : round.getOpenDate())
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(LocalDateTime.now())
                    .plusDays(ROUND_SPACING_DAYS);

            while (claimed.size() < required) {
                FantasyRound created = fantasyRoundDAO.createRound(FantasyRound.builder()
                        .roundId(UUID.randomUUID())
                        .season(season)
                        .roundNumber(nextNumber)
                        .openDate(cursor)
                        .lockDeadline(cursor.plusDays(ROUND_SPACING_DAYS - 1))
                        .endDate(cursor.plusDays(ROUND_SPACING_DAYS))
                        .status(FantasyRoundStatus.UPCOMING)
                        .build());
                claimed.add(created);

                nextNumber++;
                cursor = cursor.plusDays(ROUND_SPACING_DAYS);
            }
        }

        return claimed;
    }

    private Fixture createFixture(League league,
                                  Tournament tournament,
                                  FantasyRound round,
                                  RoundRobinScheduler.Pairing<UUID> pairing,
                                  TournamentStage stage,
                                  UUID poolId,
                                  Integer bracketSlot,
                                  int matchdayNumber) {
        return fixtureDAO.create(Fixture.builder()
                .fixtureId(UUID.randomUUID())
                .leagueId(league.getLeagueId())
                .roundId(round.getRoundId())
                .teamAId(pairing.homeTeamId())
                .teamBId(pairing.awayTeamId())
                .fixtureDate(round.getOpenDate().toLocalDate())
                .fixtureTime(DEFAULT_KICKOFF)
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
                startKnockoutStage(tournament);
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
        FantasyRound round = allocateRounds(tournament.getLeagueId(), tournament.getSeason(), 1).get(0);

        for (int slot = 0; slot < pairings.size(); slot++) {
            createFixture(league, tournament, round, pairings.get(slot), stage, null, slot, matchday);
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
        FantasyRound round = allocateRounds(tournament.getLeagueId(), tournament.getSeason(), 1).get(0);

        TournamentStage nextStage = TournamentStage.forTeamsRemaining(winners.size());
        List<RoundRobinScheduler.Pairing<UUID>> nextPairings = KnockoutBracket.nextRound(winners);
        for (int slot = 0; slot < nextPairings.size(); slot++) {
            createFixture(league, tournament, round, nextPairings.get(slot), nextStage, null, slot, matchday);
        }

        // The two beaten semi-finalists meet on the same matchday as the final.
        if (nextStage == TournamentStage.FINAL && tournament.isThirdPlacePlayoff() && losers.size() == 2) {
            createFixture(league, tournament, round,
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

    private void requireManagerOrAdmin(UUID actorUserId, League league) {
        if (actorUserId == null) {
            throw new ValidationException("An authenticated user is required.");
        }

        User user = userDAO.getUserById(actorUserId)
                .orElseThrow(() -> new AuthorisationException("An authenticated user is required."));

        if (user.getRole() == UserRole.ADMINISTRATOR) {
            return;
        }
        if (actorUserId.equals(league.getManagerUserId())) {
            return;
        }
        throw new AuthorisationException("Only the league manager or an administrator can start this league.");
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
