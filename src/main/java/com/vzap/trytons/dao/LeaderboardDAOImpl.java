package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.enums.LeaderBoardScope;
import com.vzap.trytons.model.Leaderboard;
import com.vzap.trytons.model.Ranking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeaderboardDAOImpl extends BaseDAO implements LeaderboardDAO {

    private static final Logger LOG = Logger.getLogger(LeaderboardDAOImpl.class.getName());

    @Override
    public Optional<Leaderboard> getLeaderboardByLeagueId(UUID leagueId) {
        String query = "SELECT * FROM leaderboard WHERE leagueId = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, leagueId.toString());

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    Leaderboard lb = Leaderboard.builder()
                            .leaderboardId(UUID.fromString(rs.getString("leaderboardId")))
                            .leagueId(UUID.fromString(rs.getString("leagueId")))
                            .lastUpdated(rs.getObject("lastUpdated", LocalDate.class))
                            .season(rs.getString("season"))
                            .scope(LeaderBoardScope.valueOf(rs.getString("scope")))
                            .build();

                    return Optional.of(lb);
                }
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to get leaderboard by league id " + leagueId, e);
            throw new DataAccessException("Unable to get leaderboard by league id " + leagueId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Ranking> getRankingsByLeaderboardId(UUID leaderboardId) {
        String query = "SELECT * FROM ranking WHERE leaderboardId = ?";
        List<Ranking> rankings = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, leaderboardId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ranking r = Ranking.builder()
                            .rankingId(UUID.fromString(rs.getString("rankingId")))
                            .leaderboardId(UUID.fromString(rs.getString("leaderboardId")))
                            .teamId(UUID.fromString(rs.getString("teamId")))
                            .currentRanking(rs.getInt("currentRanking"))
                            .previousRanking(rs.getInt("previousRanking"))
                            .matchesPlayed(rs.getInt("matchesPlayed"))
                            .matchesWon(rs.getInt("matchesWon"))
                            .matchesDrawn(rs.getInt("matchesDrawn"))
                            .matchesLost(rs.getInt("matchesLost"))
                            .pointsFor(rs.getInt("pointsFor"))
                            .pointsAgainst(rs.getInt("pointsAgainst"))
                            .scoreDifference(rs.getInt("scoreDifference"))
                            .leaguePoints(rs.getInt("leaguePoints"))
                            .totalFantasyPoints(rs.getInt("total_fantasy_points"))
                            .updatedAt(rs.getObject("updatedAt", LocalDateTime.class))
                            .build();
                    rankings.add(r);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to get rankings by leaderboard id.", e);
        }
        return rankings;
    }

    @Override
    public Optional<Ranking> getRankingByTeamId(UUID teamId, UUID leaderboardId) {
        String query = "SELECT * FROM ranking WHERE teamId = ? AND leaderboardId = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, teamId.toString());
            ps.setString(2, leaderboardId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ranking r = Ranking.builder()
                            .rankingId(UUID.fromString(rs.getString("rankingId")))
                            .leaderboardId(UUID.fromString(rs.getString("leaderboardId")))
                            .teamId(UUID.fromString(rs.getString("teamId")))
                            .currentRanking(rs.getInt("currentRanking"))
                            .previousRanking(rs.getInt("previousRanking"))
                            .matchesPlayed(rs.getInt("matchesPlayed"))
                            .matchesWon(rs.getInt("matchesWon"))
                            .matchesDrawn(rs.getInt("matchesDrawn"))
                            .matchesLost(rs.getInt("matchesLost"))
                            .pointsFor(rs.getInt("pointsFor"))
                            .pointsAgainst(rs.getInt("pointsAgainst"))
                            .scoreDifference(rs.getInt("scoreDifference"))
                            .leaguePoints(rs.getInt("leaguePoints"))
                            .totalFantasyPoints(rs.getInt("total_fantasy_points"))
                            .updatedAt(rs.getObject("updatedAt", LocalDateTime.class))
                            .build();

                    return Optional.of(r);
                }
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to get rankings by teamId.", e);
            throw new DataAccessException("Unable to get rankings by teamId.", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Leaderboard> getLeaderboardById(UUID leaderboardId) {
        String query = "SELECT * FROM leaderboard WHERE leaderboardId = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, leaderboardId.toString());

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    Leaderboard lb = Leaderboard.builder()
                            .leaderboardId(UUID.fromString(rs.getString("leaderboardId")))
                            .leagueId(UUID.fromString(rs.getString("leagueId")))
                            .lastUpdated(rs.getObject("lastUpdated", LocalDate.class))
                            .season(rs.getString("season"))
                            .scope(LeaderBoardScope.valueOf(rs.getString("scope")))
                            .build();

                    return Optional.of(lb);
                }
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to get leaderboard by leaderboard id " + leaderboardId, e);
            throw new DataAccessException("Unable to get leaderboard by leaderboard id " + leaderboardId, e);
        }
        return Optional.empty();
    }

    @Override
    public void saveRanking(Ranking ranking) {
        throw new UnsupportedOperationException("LeaderboardDAOImpl.saveRanking is a stub.");
    }

    @Override
    public void updateRanking(Ranking ranking) {
        throw new UnsupportedOperationException("LeaderboardDAOImpl.updateRanking is a stub.");
    }

    @Override
    public void deleteRankingByLeaderboardId(UUID leaderboardId) {
        throw new UnsupportedOperationException("LeaderboardDAOImpl.deleteRankingByLeaderboardId is a stub.");
    }

    @Override
    public void updateLeaderboard(Leaderboard leaderboard) {
        throw new UnsupportedOperationException("LeaderboardDAOImpl.updateLeaderboard is a stub.");
    }

    @Override
    public void saveLeaderboard(Leaderboard leaderboard) {
        throw new UnsupportedOperationException("LeaderboardDAOImpl.saveLeaderboard is a stub.");
    }

    @Override
    public Optional<Leaderboard> getMasterLeaderboard() {
        throw new UnsupportedOperationException("LeaderboardDAOImpl.getMasterLeaderboard is a stub.");
    }
}
