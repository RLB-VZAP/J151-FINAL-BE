package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.Leaderboard;
import com.vzap.trytons.model.Ranking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeaderboardDAOImpl extends BaseDAO implements LeaderboardDAO {

    private static final Logger LOG = Logger.getLogger(UserDAOImpl.class.getName());

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
                            .is_master_leaderboard(rs.getBoolean("is_master_leaderboard"))
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
        Ranking r = null;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, leaderboardId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r = Ranking.builder()
                            .rankingId(UUID.fromString(rs.getString("rankingId")))
                            .leaderboardId(UUID.fromString(rs.getString("leaderboardId")))
                            .teamId(UUID.fromString(rs.getString("teamId")))
                            .currentRanking(rs.getInt("currentRanking"))
                            .previousRanking(rs.getInt("previousRanking"))
                            .weeklyScore(rs.getInt("weeklyScore"))
                            .totalScore(rs.getInt("totalScore"))
                            .rankMovement(rs.getInt("rankMovement"))
                            .updatedAt(rs.getObject("updatedAt", LocalDate.class))
                            .build();
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to get rankings by leaderboard id.", e);
            throw new DataAccessException("Unable to get rankings by leaderboard id.", e);
        }
        return List.of(r);
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
                            .weeklyScore(rs.getInt("weeklyScore"))
                            .totalScore(rs.getInt("totalScore"))
                            .rankMovement(rs.getInt("rankMovement"))
                            .updatedAt(rs.getObject("updatedAt", LocalDate.class))
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
}
