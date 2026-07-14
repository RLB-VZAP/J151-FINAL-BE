package com.vzap.trytons.dao;

import com.vzap.trytons.enums.LeaderBoardScope;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.Leaderboard;
import com.vzap.trytons.model.Ranking;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class LeaderboardDAOImpl extends BaseDAO implements LeaderboardDAO {

    private static final Logger LOG = Logger.getLogger(LeaderboardDAOImpl.class.getName());

    private Leaderboard mapLeaderboard(ResultSet rs) throws SQLException {
        String leagueId = rs.getString("leagueId");
        Timestamp lastUpdated = rs.getTimestamp("lastUpdated");

        return Leaderboard.builder()
                .leaderboardId(UUID.fromString(rs.getString("leaderboardId")))
                .leagueId(leagueId == null ? null : UUID.fromString(leagueId))
                .season(rs.getString("season"))
                .scope(LeaderBoardScope.valueOf(rs.getString("scope")))
                .lastUpdated(lastUpdated == null ? null : lastUpdated.toLocalDateTime())
                .build();
    }

    private Ranking mapRanking(ResultSet rs) throws SQLException {
        int previousRanking = rs.getInt("previousRanking");
        if (rs.wasNull()) {
            previousRanking = 0;
        }
        Timestamp updatedAt = rs.getTimestamp("updatedAt");

        return Ranking.builder()
                .rankingId(UUID.fromString(rs.getString("rankingId")))
                .leaderboardId(UUID.fromString(rs.getString("leaderboardId")))
                .teamId(UUID.fromString(rs.getString("teamId")))
                .currentRanking(rs.getInt("currentRanking"))
                .previousRanking(previousRanking)
                .matchesPlayed(rs.getInt("matchesPlayed"))
                .matchesWon(rs.getInt("matchesWon"))
                .matchesDrawn(rs.getInt("matchesDrawn"))
                .matchesLost(rs.getInt("matchesLost"))
                .pointsFor(rs.getInt("pointsFor"))
                .pointsAgainst(rs.getInt("pointsAgainst"))
                .scoreDifference(rs.getInt("scoreDifference"))
                .leaguePoints(rs.getInt("leaguePoints"))
                .totalFantasyPoints(rs.getInt("total_fantasy_points"))
                .updatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime())
                .build();
    }

    @Override
    public Optional<Leaderboard> getLeaderboardByLeagueId(UUID leagueId) {
        String sql = "SELECT * FROM leaderboard WHERE leagueId = ? ORDER BY lastUpdated DESC LIMIT 1";
        return findLeaderboard(sql, leagueId.toString());
    }

    @Override
    public Optional<Leaderboard> getLeaderboardById(UUID leaderboardId) {
        return findLeaderboard("SELECT * FROM leaderboard WHERE leaderboardId = ?", leaderboardId.toString());
    }

    private Optional<Leaderboard> findLeaderboard(String sql, String value) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapLeaderboard(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve leaderboard.", e);
            throw new DataAccessException("Unable to retrieve leaderboard.", e);
        }
    }

    @Override
    public List<Ranking> getRankingsByLeaderboardId(UUID leaderboardId) {
        List<Ranking> rankings = new ArrayList<>();
        String sql = "SELECT * FROM ranking WHERE leaderboardId = ? ORDER BY currentRanking";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, leaderboardId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rankings.add(mapRanking(resultSet));
                }
            }
            return rankings;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve leaderboard rankings.", e);
            throw new DataAccessException("Unable to retrieve leaderboard rankings.", e);
        }
    }

    @Override
    public Optional<Ranking> getRankingByTeamId(UUID teamId, UUID leaderboardId) {
        String sql = "SELECT * FROM ranking WHERE teamId = ? AND leaderboardId = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, teamId.toString());
            statement.setString(2, leaderboardId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRanking(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve team ranking.", e);
            throw new DataAccessException("Unable to retrieve team ranking.", e);
        }
    }

    @Override
    public void saveRanking(Ranking ranking) {
        String sql = """
                INSERT INTO ranking
                    (rankingId, leaderboardId, teamId, currentRanking, previousRanking,
                     matchesPlayed, matchesWon, matchesDrawn, matchesLost, pointsFor,
                     pointsAgainst, leaguePoints, total_fantasy_points, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        executeRankingWrite(sql, ranking, false);
    }

    @Override
    public void updateRanking(Ranking ranking) {
        String sql = """
                UPDATE ranking
                   SET currentRanking = ?, previousRanking = ?, matchesPlayed = ?,
                       matchesWon = ?, matchesDrawn = ?, matchesLost = ?, pointsFor = ?,
                       pointsAgainst = ?, leaguePoints = ?, total_fantasy_points = ?, updatedAt = ?
                 WHERE rankingId = ?
                """;
        executeRankingWrite(sql, ranking, true);
    }

    private void executeRankingWrite(String sql, Ranking ranking, boolean update) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            LocalDateTime updatedAt = ranking.getUpdatedAt() == null ? LocalDateTime.now() : ranking.getUpdatedAt();
            if (!update) {
                statement.setString(1, ranking.getRankingId().toString());
                statement.setString(2, ranking.getLeaderboardId().toString());
                statement.setString(3, ranking.getTeamId().toString());
                statement.setInt(4, ranking.getCurrentRanking());
                setNullablePreviousRank(statement, 5, ranking.getPreviousRanking());
                statement.setInt(6, ranking.getMatchesPlayed());
                statement.setInt(7, ranking.getMatchesWon());
                statement.setInt(8, ranking.getMatchesDrawn());
                statement.setInt(9, ranking.getMatchesLost());
                statement.setInt(10, ranking.getPointsFor());
                statement.setInt(11, ranking.getPointsAgainst());
                statement.setInt(12, ranking.getLeaguePoints());
                statement.setInt(13, ranking.getTotalFantasyPoints());
                statement.setTimestamp(14, Timestamp.valueOf(updatedAt));
            } else {
                statement.setInt(1, ranking.getCurrentRanking());
                setNullablePreviousRank(statement, 2, ranking.getPreviousRanking());
                statement.setInt(3, ranking.getMatchesPlayed());
                statement.setInt(4, ranking.getMatchesWon());
                statement.setInt(5, ranking.getMatchesDrawn());
                statement.setInt(6, ranking.getMatchesLost());
                statement.setInt(7, ranking.getPointsFor());
                statement.setInt(8, ranking.getPointsAgainst());
                statement.setInt(9, ranking.getLeaguePoints());
                statement.setInt(10, ranking.getTotalFantasyPoints());
                statement.setTimestamp(11, Timestamp.valueOf(updatedAt));
                statement.setString(12, ranking.getRankingId().toString());
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to save ranking.", e);
            throw new DataAccessException("Unable to save ranking.", e);
        }
    }

    private void setNullablePreviousRank(PreparedStatement statement, int index, int previousRank) throws SQLException {
        if (previousRank <= 0) {
            statement.setNull(index, java.sql.Types.INTEGER);
        } else {
            statement.setInt(index, previousRank);
        }
    }

    @Override
    public void deleteRankingByLeaderboardId(UUID leaderboardId) {
        executeUpdate("DELETE FROM ranking WHERE leaderboardId = ?", leaderboardId.toString());
    }

    @Override
    public void updateLeaderboard(Leaderboard leaderboard) {
        String sql = "UPDATE leaderboard SET leagueId = ?, season = ?, scope = ?, lastUpdated = ? WHERE leaderboardId = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (leaderboard.getLeagueId() == null) {
                statement.setNull(1, java.sql.Types.VARCHAR);
            } else {
                statement.setString(1, leaderboard.getLeagueId().toString());
            }
            statement.setString(2, leaderboard.getSeason());
            statement.setString(3, leaderboard.getScope().name());
            statement.setTimestamp(4, Timestamp.valueOf(leaderboard.getLastUpdated() == null ? LocalDateTime.now() : leaderboard.getLastUpdated()));
            statement.setString(5, leaderboard.getLeaderboardId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update leaderboard.", e);
            throw new DataAccessException("Unable to update leaderboard.", e);
        }
    }

    @Override
    public void saveLeaderboard(Leaderboard leaderboard) {
        String sql = "INSERT INTO leaderboard (leaderboardId, leagueId, season, scope, lastUpdated) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, leaderboard.getLeaderboardId().toString());
            if (leaderboard.getLeagueId() == null) {
                statement.setNull(2, java.sql.Types.VARCHAR);
            } else {
                statement.setString(2, leaderboard.getLeagueId().toString());
            }
            statement.setString(3, leaderboard.getSeason());
            statement.setString(4, leaderboard.getScope().name());
            statement.setTimestamp(5, Timestamp.valueOf(leaderboard.getLastUpdated() == null ? LocalDateTime.now() : leaderboard.getLastUpdated()));
            statement.executeUpdate();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to save leaderboard.", e);
            throw new DataAccessException("Unable to save leaderboard.", e);
        }
    }

    @Override
    public Optional<Leaderboard> getMasterLeaderboard() {
        String sql = "SELECT * FROM leaderboard WHERE scope = 'MASTER' ORDER BY lastUpdated DESC LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return resultSet.next() ? Optional.of(mapLeaderboard(resultSet)) : Optional.empty();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve master leaderboard.", e);
            throw new DataAccessException("Unable to retrieve master leaderboard.", e);
        }
    }

    private void executeUpdate(String sql, String value) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, value);
            statement.executeUpdate();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update leaderboard data.", e);
            throw new DataAccessException("Unable to update leaderboard data.", e);
        }
    }
}
