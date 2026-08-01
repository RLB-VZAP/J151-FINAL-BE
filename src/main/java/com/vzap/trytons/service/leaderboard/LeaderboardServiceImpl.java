package com.vzap.trytons.service.leaderboard;

import com.vzap.trytons.dao.fantasyteam.FantasyTeamDAO;
import com.vzap.trytons.dao.leaderboard.LeaderboardAggregationDAO;
import com.vzap.trytons.dao.leaderboard.LeaderboardDAO;
import com.vzap.trytons.dao.league.LeagueDAO;
import com.vzap.trytons.dao.league.LeagueMembershipDAO;
import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.leaderboard.LeaderboardRefreshResultDTO;
import java.util.UUID;

import com.vzap.trytons.dto.leaderboard.LeaderboardEntryResponseDTO;
import com.vzap.trytons.enums.LeaderboardScope;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.league.League;
import com.vzap.trytons.model.leaderboard.Leaderboard;
import com.vzap.trytons.model.leaderboard.Ranking;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.service.notification.NotificationService;
import com.vzap.trytons.service.shared.SeasonResolver;
import com.vzap.trytons.util.LeaderboardAggregator;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LeaderboardServiceImpl implements LeaderboardService{

    private static final Logger LOG = Logger.getLogger(LeaderboardServiceImpl.class.getName());

    @Inject
    private LeaderboardDAO leaderboardDAO;
    @Inject
    private LeaderboardAggregationDAO leaderboardAggregationDAO;
    @Inject
    private LeagueMembershipDAO leagueMembershipDAO;
    @Inject
    private FantasyTeamDAO  fantasyTeamDAO;
    @Inject
    private LeagueDAO leagueDAO;
    @Inject
    private UserDAO userDAO;
    @Inject
    private SeasonResolver seasonResolver;
    @Inject
    private NotificationService notificationService;

    //New added methods
    //================================================================================================================================================

    @Override
    public LeaderboardRefreshResultDTO refreshLeagueLeaderboard(UUID actorUserId, UUID leagueId) {
        requireLeagueManagerOrAdmin(actorUserId, leagueId);
        String season = seasonResolver.resolveCurrentSeason();
        Leaderboard board = ensureLeagueLeaderboard(leagueId, season);
        // No leagueType filter here: a private league's own table must still
        // count its own matches, only the master total excludes them.
        List<LeaderboardAggregator.FixtureScoreRow> rows =
                leaderboardAggregationDAO.findLeagueScoreRows(season, leagueId);
        applyAggregation(board, LeaderboardAggregator.aggregate(rows, LeaderboardAggregator.Scope.LEAGUE));
        return refreshRankings(board);
    }

    /**
     * Get-or-create for a LEAGUE-scope board. Covers two gaps createLeague's
     * best-effort insert can leave open: a league created before any fantasy
     * round existed (createLeague swallowed that failure rather than
     * blocking the league) and a league surviving into a new season, since
     * leaderboard is unique on (scopeKey, season) and needs a fresh row per
     * season. A concurrent refresh racing this insert loses the unique key
     * and is resolved by re-reading rather than failing.
     */
    private Leaderboard ensureLeagueLeaderboard(UUID leagueId, String season) {
        Optional<Leaderboard> existing = leaderboardDAO.getLeaderboardByLeagueAndSeason(leagueId, season);
        if (existing.isPresent()) {
            return existing.get();
        }

        Leaderboard board = Leaderboard.builder()
                .leaderboardId(UUID.randomUUID())
                .leagueId(leagueId)
                .season(season)
                .scope(LeaderboardScope.LEAGUE)
                .lastUpdated(LocalDateTime.now())
                .build();
        try {
            leaderboardDAO.saveLeaderboard(board);
            return board;
        } catch (ConflictException e) {
            return leaderboardDAO.getLeaderboardByLeagueAndSeason(leagueId, season).orElseThrow(() -> e);
        }
    }

    @Override
    public LeaderboardRefreshResultDTO refreshOverallLeaderboard(UUID actorUserId) {
        requireAdmin(actorUserId);
        String season = seasonResolver.resolveCurrentSeason();
        Optional<Leaderboard> master = leaderboardDAO.getMasterLeaderboard(season);
        if (master.isEmpty()) {
            return LeaderboardRefreshResultDTO.builder()
                    .success(false)
                    .message("No master leaderboard exists for season " + season + ".")
                    .teamsProcessed(0)
                    .rankingsUpdated(0)
                    .build();
        }
        Leaderboard board = master.get();
        List<LeaderboardAggregator.FixtureScoreRow> rows = leaderboardAggregationDAO.findMasterScoreRows(season);
        applyAggregation(board, LeaderboardAggregator.aggregate(rows, LeaderboardAggregator.Scope.MASTER));
        return refreshRankings(board);
    }

    /**
     * Writes rolled-up totals onto {@code ranking} before {@link #refreshRankings}
     * re-sorts and renumbers. This is a full recompute, not a merge:
     * {@link LeaderboardAggregator#reconcileWithExisting} guarantees an
     * entry -- zeroed if the aggregation found nothing -- for every team
     * this leaderboard already tracks, so a team whose only results are
     * private (excluded at MASTER scope) gets its old total overwritten with
     * zero instead of keeping it forever. A team with no prior ranking row
     * (its first-ever counted result) is inserted via the previously-unused
     * {@code saveRanking}; currentRanking on that new row only needs to be
     * provisionally unique, since refreshRankings renumbers everything,
     * including these new rows, immediately afterwards.
     */
    private void applyAggregation(Leaderboard board, List<LeaderboardAggregator.TeamTotals> totals) {
        List<Ranking> existingRankings = leaderboardDAO.getRankingsByLeaderboardId(board.getLeaderboardId());
        Map<UUID, Ranking> existingByTeam = new HashMap<>();
        for (Ranking ranking : existingRankings) {
            existingByTeam.put(ranking.getTeamId(), ranking);
        }

        Map<UUID, LeaderboardAggregator.TeamTotals> reconciled =
                LeaderboardAggregator.reconcileWithExisting(totals, existingByTeam.keySet());

        LocalDateTime now = LocalDateTime.now();
        int nextProvisionalRank = existingRankings.size() + 1;

        for (LeaderboardAggregator.TeamTotals teamTotals : reconciled.values()) {
            Ranking ranking = existingByTeam.get(teamTotals.getTeamId());
            if (ranking == null) {
                ranking = Ranking.builder()
                        .rankingId(UUID.randomUUID())
                        .leaderboardId(board.getLeaderboardId())
                        .teamId(teamTotals.getTeamId())
                        .currentRanking(nextProvisionalRank++)
                        .build();
                copyTotals(ranking, teamTotals, now);
                leaderboardDAO.saveRanking(ranking);
            } else {
                copyTotals(ranking, teamTotals, now);
                leaderboardDAO.updateRanking(ranking);
            }
        }
    }

    private void copyTotals(Ranking ranking, LeaderboardAggregator.TeamTotals totals, LocalDateTime now) {
        ranking.setMatchesPlayed(totals.getMatchesPlayed());
        ranking.setMatchesWon(totals.getMatchesWon());
        ranking.setMatchesDrawn(totals.getMatchesDrawn());
        ranking.setMatchesLost(totals.getMatchesLost());
        ranking.setPointsFor(totals.getPointsFor());
        ranking.setPointsAgainst(totals.getPointsAgainst());
        ranking.setLeaguePoints(totals.getLeaguePoints());
        ranking.setTotalFantasyPoints(totals.getTotalFantasyPoints());
        ranking.setUpdatedAt(now);
    }

    @Override
    public List<LeaderboardEntryResponseDTO> getOverallLeaderboard(UUID actorUserId) {
        String season = seasonResolver.resolveCurrentSeason();
        Optional<Leaderboard> master = leaderboardDAO.getMasterLeaderboard(season);
        if (master.isEmpty()) {
            return Collections.emptyList();
        }

        List<LeaderboardEntryResponseDTO> entries = new ArrayList<>();
        for (Ranking ranking : leaderboardDAO.getRankingsByLeaderboardId(master.get().getLeaderboardId())) {
            FantasyTeam team = fantasyTeamDAO.findTeamById(ranking.getTeamId());
            if (team == null) {
                continue;
            }

            String ownerUsername = userDAO.getUserById(team.getOwnerUserId())
                    .map(User::getUsername)
                    .orElse(null);

            entries.add(LeaderboardEntryResponseDTO.builder().teamId(ranking.getTeamId())
                    .teamName(team.getTeamName())
                    .owner(ownerUsername)
                    .rank(ranking.getCurrentRanking())
                    .rankMovement(calculateRankMovement(ranking.getCurrentRanking(), ranking.getPreviousRanking()))
                    .previousRanking(ranking.getPreviousRanking())
                    .matchesPlayed(ranking.getMatchesPlayed())
                    .matchesWon(ranking.getMatchesWon())
                    .matchesDrawn(ranking.getMatchesDrawn())
                    .matchesLost(ranking.getMatchesLost())
                    .pointsFor(ranking.getPointsFor())
                    .pointsAgainst(ranking.getPointsAgainst())
                    .pointsDifference(ranking.getScoreDifference())
                    .leaguePoints(ranking.getLeaguePoints())
                    .totalFantasyPoints(ranking.getTotalFantasyPoints())
                    .build());
        }
        return entries;
    }

    @Override
    public List<LeaderboardEntryResponseDTO> getPublicOverallLeaderboard(int limit) {
        // getOverallLeaderboard does not use the actor id (it reads only the master
        // leaderboard), so a null actor is safe here and keeps this preview user-less.
        List<LeaderboardEntryResponseDTO> all = getOverallLeaderboard(null);
        if (limit > 0 && all.size() > limit) {
            return new ArrayList<>(all.subList(0, limit));
        }
        return all;
    }

    @Override
    public List<LeaderboardEntryResponseDTO> getLeaderboardForLeague(UUID leagueId, UUID requestingUserId) throws AuthorisationException {
        if (!leagueMembershipDAO.existsActiveByLeagueAndUser(leagueId, requestingUserId)){
            throw new AuthorisationException("FORBIDDEN");
        }

        Optional<Leaderboard> leaderboard = leaderboardDAO.getLeaderboardByLeagueId(leagueId);
        if (leaderboard.isEmpty()) {
            return Collections.emptyList();
        }

        List<Ranking> rankingList = leaderboardDAO.getRankingsByLeaderboardId(leaderboard.get().getLeaderboardId());
        List<LeaderboardEntryResponseDTO> leaderboardEntryResponseDTOList = new ArrayList<>();
        for (Ranking ranking : rankingList) {
            FantasyTeam team = fantasyTeamDAO.findTeamById(ranking.getTeamId());
            if (team == null) {
                continue;
            }
            String ownerUsername = userDAO.getUserById(team.getOwnerUserId())
                    .map(User::getUsername)
                    .orElse(null);
            LeaderboardEntryResponseDTO dto = LeaderboardEntryResponseDTO.builder()
                    .teamId(ranking.getTeamId())
                    .teamName(team.getTeamName())
                    .owner(ownerUsername)
                    .rank(ranking.getCurrentRanking())
                    .rankMovement(calculateRankMovement(ranking.getCurrentRanking(), ranking.getPreviousRanking()))
                    .previousRanking(ranking.getPreviousRanking())
                    .matchesPlayed(ranking.getMatchesPlayed())
                    .matchesWon(ranking.getMatchesWon())
                    .matchesDrawn(ranking.getMatchesDrawn())
                    .matchesLost(ranking.getMatchesLost())
                    .pointsFor(ranking.getPointsFor())
                    .pointsAgainst(ranking.getPointsAgainst())
                    .pointsDifference(ranking.getScoreDifference())
                    .leaguePoints(ranking.getLeaguePoints())
                    .totalFantasyPoints(ranking.getTotalFantasyPoints())
                    .build();

            leaderboardEntryResponseDTOList.add(dto);
        }
        return leaderboardEntryResponseDTOList;
    }

    @Override
    public Optional<LeaderboardEntryResponseDTO> getRankingForTeam(UUID teamId, UUID leaderboardId, UUID requestingUserId) throws AuthorisationException {
        if (leaderboardId == null) {
            throw new ValidationException("Leaderboard ID is required.");
        }
        Optional<Leaderboard> l = leaderboardDAO.getLeaderboardById(leaderboardId);
        if (l.isEmpty()) {
            return Optional.empty();
        }
        if (!leagueMembershipDAO.existsActiveByLeagueAndUser(l.get().getLeagueId(), requestingUserId)){
            throw new AuthorisationException("FORBIDDEN");
        }

        Optional<Ranking> r = leaderboardDAO.getRankingByTeamId(teamId, leaderboardId);
        if (r.isPresent()) {
            FantasyTeam team = fantasyTeamDAO.findTeamById(r.get().getTeamId());
            if (team == null) {
                return Optional.empty();
            }
            Ranking ranking = r.get();
            String ownerUsername = userDAO.getUserById(team.getOwnerUserId())
                    .map(User::getUsername)
                    .orElse(null);
            LeaderboardEntryResponseDTO dto = LeaderboardEntryResponseDTO.builder()
                    .teamId(ranking.getTeamId())
                    .teamName(team.getTeamName())
                    .owner(ownerUsername)
                    .rank(ranking.getCurrentRanking())
                    .rankMovement(calculateRankMovement(ranking.getCurrentRanking(), ranking.getPreviousRanking()))
                    .previousRanking(ranking.getPreviousRanking())
                    .matchesPlayed(ranking.getMatchesPlayed())
                    .matchesWon(ranking.getMatchesWon())
                    .matchesDrawn(ranking.getMatchesDrawn())
                    .matchesLost(ranking.getMatchesLost())
                    .pointsFor(ranking.getPointsFor())
                    .pointsAgainst(ranking.getPointsAgainst())
                    .pointsDifference(ranking.getScoreDifference())
                    .leaguePoints(ranking.getLeaguePoints())
                    .totalFantasyPoints(ranking.getTotalFantasyPoints())
                    .build();

            return Optional.of(dto);
        }
        return Optional.empty();
    }

    private LeaderboardRefreshResultDTO refreshRankings(Leaderboard leaderboard) {
        List<Ranking> rankings = leaderboardDAO.getRankingsByLeaderboardId(leaderboard.getLeaderboardId());
        rankings.sort(Comparator.comparingInt(Ranking::getLeaguePoints).reversed()
                .thenComparing(Comparator.comparingInt(Ranking::getScoreDifference).reversed())
                .thenComparing(Comparator.comparingInt(Ranking::getTotalFantasyPoints).reversed()));

        LocalDateTime now = LocalDateTime.now();

        int temporaryBase = rankings.size();
        for (int i = 0; i < rankings.size(); i++) {
            Ranking ranking = rankings.get(i);
            ranking.setPreviousRanking(ranking.getCurrentRanking());
            ranking.setCurrentRanking(temporaryBase + i + 1);
            ranking.setUpdatedAt(now);
            leaderboardDAO.updateRanking(ranking);
        }

        int position = 1;
        for (Ranking ranking : rankings) {
            ranking.setCurrentRanking(position);
            ranking.setUpdatedAt(now);
            leaderboardDAO.updateRanking(ranking);
            position++;
        }

        leaderboard.setLastUpdated(now);
        leaderboardDAO.updateLeaderboard(leaderboard);

        notifyRankChanges(leaderboard, rankings);

        return LeaderboardRefreshResultDTO.builder()
                .success(true)
                .message("Leaderboard refreshed successfully.")
                .teamsProcessed(rankings.size())
                .rankingsUpdated(rankings.size())
                .build();
    }

    private void notifyRankChanges(Leaderboard leaderboard, List<Ranking> rankings) {
        if (leaderboard.getLeagueId() == null) {
            return;
        }
        try {
            for (Ranking ranking : rankings) {
                if (ranking.getTeamId() == null) {
                    continue;
                }
                Integer previousRanking = ranking.getPreviousRanking();
                if (previousRanking != null && previousRanking == ranking.getCurrentRanking()) {
                    continue;
                }
                fantasyTeamDAO.getTeamById(ranking.getTeamId())
                        .map(FantasyTeam::getOwnerUserId)
                        .ifPresent(ownerId -> notificationService.notifyLeaderboardChange(ownerId, leaderboard.getLeagueId(), ranking.getCurrentRanking()));
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to send leaderboard-change notifications for leaderboard " + leaderboard.getLeaderboardId(), e);
        }
    }

    private void requireLeagueManagerOrAdmin(UUID actorUserId, UUID leagueId) {
        if (actorUserId == null) {
            throw new AuthorisationException("Authentication required.");
        }

        User actor = userDAO.getUserById(actorUserId).orElseThrow(() -> new AuthorisationException("Authentication required."));

        if (actor.getRole() == UserRole.ADMINISTRATOR) {
            return;
        }
        League league = leagueDAO.findLeagueById(leagueId).orElseThrow(() -> new ResourceNotFoundException("League not found."));

        if (!actorUserId.equals(league.getManagerUserId())) {
            throw new AuthorisationException("Only the league manager or an administrator may refresh this leaderboard.");
        }
    }

    private void requireAdmin(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated administrator is required.");
        }

        User actor = userDAO.getUserById(actorUserId).orElseThrow(() -> new AuthorisationException("An authenticated administrator is required."));

        if (actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new AuthorisationException("Only administrators may refresh leaderboards.");
        }
    }

    private Integer calculateRankMovement(int currentRanking, Integer previousRanking) {
        if (previousRanking != null){
            return previousRanking - currentRanking;
        } else return null;
    }
}