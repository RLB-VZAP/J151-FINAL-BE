package com.vzap.trytons.service;

import com.vzap.trytons.dto.LeaderboardRefreshResultDTO;
import java.util.UUID;
import com.vzap.trytons.dao.FantasyTeamDAO;
import com.vzap.trytons.dao.LeaderboardDAO;
import com.vzap.trytons.dao.LeagueDAO;
import com.vzap.trytons.dao.LeagueMembershipDAO;
import com.vzap.trytons.dto.LeaderboardEntryResponseDTO;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Leaderboard;
import com.vzap.trytons.model.Ranking;
import jakarta.inject.Inject;
import java.util.*;

public class LeaderboardServiceImpl implements LeaderboardService{
    @Inject
    private LeaderboardDAO leaderboardDAO;
    @Inject
    private LeagueMembershipDAO leagueMembershipDAO;
    @Inject
    private FantasyTeamDAO  fantasyTeamDAO;
    @Inject
    private LeagueDAO leagueDAO;

    //New added methods
    //================================================================================================================================================

    @Override
    public LeaderboardRefreshResultDTO refreshLeagueLeaderboard(UUID actorUserId, UUID leagueId) {
        return null;
    }

    @Override
    public LeaderboardRefreshResultDTO refreshOverallLeaderboard(UUID actorUserId) {
        return null;
    }

    @Override
    public List<LeaderboardEntryResponseDTO> getOverallLeaderboard(UUID actorUserId) {
        return List.of();
    }

    //================================================================================================================================================

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
            LeaderboardEntryResponseDTO dto = LeaderboardEntryResponseDTO.builder()
                    .teamId(ranking.getTeamId())
                    .teamName(team.getTeamName())
                    .owner(team.getOwner().getUsername())
                    .rank(ranking.getCurrentRanking())
                    .rankMovement(calculateRankMovement(ranking.getCurrentRanking(), ranking.getPreviousRanking()))
                    .previousRanking(ranking.getPreviousRanking())
                    .matchesPlayed(ranking.getMatchesPlayed())
                    .matchesWon(ranking.getMatchesWon())
                    .matchesDrawn(ranking.getMatchesDrawn())
                    .matchesLost(ranking.getMatchesLost())
                    .pointsFor(ranking.getPointsFor())
                    .pointsAgainst(ranking.getPointsAgainst())
                    .scoreDifference(ranking.getScoreDifference())
                    .leaguePoints(ranking.getLeaguePoints())
                    .totalFantasyPoints(ranking.getTotal_fantasy_points())
                    .build();

            leaderboardEntryResponseDTOList.add(dto);
        }
        return leaderboardEntryResponseDTOList;
    }

    @Override
    public Optional<LeaderboardEntryResponseDTO> getRankingForTeam(UUID teamId, UUID leaderboardId, UUID requestingUserId) throws AuthorisationException {
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
            LeaderboardEntryResponseDTO dto = LeaderboardEntryResponseDTO.builder()
                    .teamId(team.getTeamId())
                    .teamName(team.getTeamName())
                    .owner(team.getOwner().getUsername())
                    .rank(ranking.getCurrentRanking())
                    .rankMovement(calculateRankMovement(ranking.getCurrentRanking(), ranking.getPreviousRanking()))
                    .previousRanking(ranking.getPreviousRanking())
                    .matchesPlayed(ranking.getMatchesPlayed())
                    .matchesWon(ranking.getMatchesWon())
                    .matchesDrawn(ranking.getMatchesDrawn())
                    .matchesLost(ranking.getMatchesLost())
                    .pointsFor(ranking.getPointsFor())
                    .pointsAgainst(ranking.getPointsAgainst())
                    .scoreDifference(ranking.getScoreDifference())
                    .leaguePoints(ranking.getLeaguePoints())
                    .totalFantasyPoints(ranking.getTotal_fantasy_points())
                    .build();

            return Optional.of(dto);
        }
        return Optional.empty();
    }

    //Small private helper method for calculating the rank movement:
    private Integer calculateRankMovement(int currentRanking, Integer previousRanking) {
        if (previousRanking != null){
            return previousRanking - currentRanking;
        }else return null;
    }
}