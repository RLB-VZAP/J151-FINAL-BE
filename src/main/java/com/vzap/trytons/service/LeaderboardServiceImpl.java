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
import com.vzap.trytons.model.League;
import com.vzap.trytons.model.Ranking;
import jakarta.inject.Inject;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeaderboardServiceImpl implements LeaderboardService{
    @Inject
    private LeaderboardDAO leaderboardDAO;
    @Inject
    private LeagueMembershipDAO leagueMembershipDAO;
    @Inject
    private FantasyTeamDAO  fantasyTeamDAO;
    @Inject
    private LeagueDAO leagueDAO;

    private static final Logger LOG = Logger.getLogger(LeaderboardServiceImpl.class.getName());
    
    @Override
    public LeaderboardRefreshResultDTO refreshLeagueLeaderboard(UUID actorUserId, UUID leagueId) {
        throw new UnsupportedOperationException("LeaderboardServiceImpl.refreshLeagueLeaderboard is a stub.");
    }

    @Override
    public LeaderboardRefreshResultDTO refreshOverallLeaderboard(UUID actorUserId) {
        throw new UnsupportedOperationException("LeaderboardServiceImpl.refreshOverallLeaderboard is a stub.");
    }

    @Override
    public List<LeaderboardEntryResponseDTO> getOverallLeaderboard(UUID actorUserId) {
        throw new UnsupportedOperationException("LeaderboardServiceImpl.getOverallLeaderboard is a stub.");
    }

    @Override
    public List<LeaderboardEntryResponseDTO> getLeaderboardForLeague(UUID leagueId, UUID requestingUserId) throws AuthorisationException {
        try {
            if (!leagueMembershipDAO.existsActiveByLeagueAndUser(leagueId, requestingUserId)){
                throw new AuthorisationException("FORBIDDEN");
            }

            //Checks whether the league is private/may be viewed.
            //Optional<League> league = leagueDAO.findById(leagueId);
            //if (league.isPrivate() && !leagueMembershipDAO.existsActiveByLeagueAndUser(leagueId, requestingUserId)) {
            //throw new AuthorisationException("FORBIDDEN")
            //}

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
                        .build();

                leaderboardEntryResponseDTOList.add(dto);
            }
            return leaderboardEntryResponseDTOList;
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Error checking league membership", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<LeaderboardEntryResponseDTO> getRankingForTeam(UUID teamId, UUID leaderboardId, UUID requestingUserId) throws AuthorisationException {
        try {
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
                LeaderboardEntryResponseDTO dto = LeaderboardEntryResponseDTO.builder()
                        .teamId(team.getTeamId())
                        .teamName(team.getTeamName())
                        .owner(team.getOwner().getUsername())
                        .rank(r.get().getCurrentRanking())
                        .previousRank(r.get().getPreviousRanking())
                        .matchesPlayed(r.get().getMatchesPlayed())
                        .matchesWon(r.get().getMatchesWon())
                        .matchesDrawn(r.get().getMatchesDrawn())
                        .matchesLost(r.get().getMatchesLost())
                        .pointsFor(r.get().getPointsFor())
                        .pointsAgainst(r.get().getPointsAgainst())
                        .scoreDifference(r.get().getScoreDifference())
                        .leaguePoints(r.get().getLeaguePoints())
                        .totalFantasyPoints(r.get().getTotalFantasyPoints())
                        .build();

                return Optional.of(dto);
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Error checking league membership", e);
            return Optional.empty();
        }
        return Optional.empty();
    }
}
