package com.vzap.trytons.service;

import com.vzap.trytons.dao.FantasyTeamDAO;
import com.vzap.trytons.dao.LeaderboardDAO;
import com.vzap.trytons.dao.LeagueMembershipDAO;
import com.vzap.trytons.dto.LeaderboardEntryResponseDTO;
import com.vzap.trytons.dto.LeaderboardRefreshResultDTO;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Leaderboard;
import com.vzap.trytons.model.Ranking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class LeaderboardServiceImpl implements LeaderboardService {

    @Inject
    private LeaderboardDAO leaderboardDAO;

    @Inject
    private LeagueMembershipDAO leagueMembershipDAO;

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Override
    public LeaderboardRefreshResultDTO refreshLeagueLeaderboard(UUID actorUserId, UUID leagueId) {
        return LeaderboardRefreshResultDTO.builder()
                .success(false)
                .message("Leaderboard recalculation is not part of the current demo build.")
                .teamsProcessed(0)
                .rankingsUpdated(0)
                .build();
    }

    @Override
    public LeaderboardRefreshResultDTO refreshOverallLeaderboard(UUID actorUserId) {
        return LeaderboardRefreshResultDTO.builder()
                .success(false)
                .message("Overall leaderboard recalculation is not part of the current demo build.")
                .teamsProcessed(0)
                .rankingsUpdated(0)
                .build();
    }

    @Override
    public List<LeaderboardEntryResponseDTO> getOverallLeaderboard(UUID actorUserId) {
        return leaderboardDAO.getMasterLeaderboard()
                .map(this::mapLeaderboard)
                .orElseGet(Collections::emptyList);
    }

    @Override
    public List<LeaderboardEntryResponseDTO> getLeaderboardForLeague(UUID leagueId, UUID requestingUserId) {
        if (!leagueMembershipDAO.existsActiveByLeagueAndUser(leagueId, requestingUserId)) {
            throw new AuthorisationException("You must be a member of this league to view its leaderboard.");
        }

        return leaderboardDAO.getLeaderboardByLeagueId(leagueId)
                .map(this::mapLeaderboard)
                .orElseGet(Collections::emptyList);
    }

    @Override
    public Optional<LeaderboardEntryResponseDTO> getRankingForTeam(
            UUID teamId,
            UUID leaderboardId,
            UUID requestingUserId) {

        Optional<Leaderboard> leaderboard = leaderboardDAO.getLeaderboardById(leaderboardId);
        if (leaderboard.isEmpty()) {
            return Optional.empty();
        }

        UUID leagueId = leaderboard.get().getLeagueId();
        if (leagueId != null && !leagueMembershipDAO.existsActiveByLeagueAndUser(leagueId, requestingUserId)) {
            throw new AuthorisationException("You must be a member of this league to view this ranking.");
        }

        return leaderboardDAO.getRankingByTeamId(teamId, leaderboardId)
                .flatMap(this::mapRanking);
    }

    private List<LeaderboardEntryResponseDTO> mapLeaderboard(Leaderboard leaderboard) {
        List<LeaderboardEntryResponseDTO> entries = new ArrayList<>();
        for (Ranking ranking : leaderboardDAO.getRankingsByLeaderboardId(leaderboard.getLeaderboardId())) {
            mapRanking(ranking).ifPresent(entries::add);
        }
        return entries;
    }

    private Optional<LeaderboardEntryResponseDTO> mapRanking(Ranking ranking) {
        FantasyTeam team = fantasyTeamDAO.findTeamById(ranking.getTeamId());
        if (team == null) {
            return Optional.empty();
        }

        String ownerName = team.getOwner() == null ? null : team.getOwner().getUsername();
        return Optional.of(LeaderboardEntryResponseDTO.builder()
                .teamId(ranking.getTeamId())
                .teamName(team.getTeamName())
                .owner(ownerName)
                .rank(ranking.getCurrentRanking())
                .previousRank(ranking.getPreviousRanking())
                .matchesPlayed(ranking.getMatchesPlayed())
                .matchesWon(ranking.getMatchesWon())
                .matchesDrawn(ranking.getMatchesDrawn())
                .matchesLost(ranking.getMatchesLost())
                .pointsFor(ranking.getPointsFor())
                .pointsAgainst(ranking.getPointsAgainst())
                .scoreDifference(ranking.getScoreDifference())
                .leaguePoints(ranking.getLeaguePoints())
                .totalFantasyPoints(ranking.getTotalFantasyPoints())
                .build());
    }
}
