package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.PlayerStatistics;
import jakarta.inject.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class PlayerStatisticsDAOImpl extends BaseDAO implements PlayerStatisticsDAO {

    private static final Logger LOG = Logger.getLogger(PlayerStatisticsDAOImpl.class.getName());

    private static final String PLAYER_STATISTICS_SELECT = "SELECT statId, resultId, teamId, playerId, tries, assists, tackles, missedTackles, conversions, penalties, metersGained, yellowCards, redCards, statisticDate FROM playerStatistics ";

    private PlayerStatistics mapPlayerStatistics(ResultSet rs) throws SQLException {
        PlayerStatistics statistics = new PlayerStatistics();
        statistics.setStatId(UUID.fromString(rs.getString("statId")));
        statistics.setResultId(UUID.fromString(rs.getString("resultId")));
        statistics.setTeamId(UUID.fromString(rs.getString("teamId")));
        statistics.setPlayerId(UUID.fromString(rs.getString("playerId")));
        statistics.setTries(rs.getInt("tries"));
        statistics.setAssists(rs.getInt("assists"));
        statistics.setTackles(rs.getInt("tackles"));
        statistics.setMissedTackles(rs.getInt("missedTackles"));
        statistics.setConversions(rs.getInt("conversions"));
        statistics.setPenalties(rs.getInt("penalties"));
        statistics.setMetersGained(rs.getInt("metersGained"));
        statistics.setYellowCards(rs.getInt("yellowCards"));
        statistics.setRedCards(rs.getInt("redCards"));

        Timestamp statisticDate = rs.getTimestamp("statisticDate");
        statistics.setStatisticDate(statisticDate != null ? statisticDate.toLocalDateTime() : null);

        return statistics;
    }

    @Override
    public List<PlayerStatistics> findByResultId(UUID resultId) {
        String query = PLAYER_STATISTICS_SELECT + "WHERE resultId = ? ORDER BY teamId ASC, playerId ASC";

        List<PlayerStatistics> statistics = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, resultId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    statistics.add(mapPlayerStatistics(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve player statistics by result ID.", e);
            throw new DataAccessException("Unable to retrieve player statistics by result ID.", e);
        }

        return statistics;
    }

    @Override
    public List<PlayerStatistics> findByResultIdAndTeamId(UUID resultId, UUID teamId) {
        String query = PLAYER_STATISTICS_SELECT + "WHERE resultId = ? AND teamId = ? ORDER BY playerId ASC";

        List<PlayerStatistics> statistics = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, resultId.toString());
            ps.setString(2, teamId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    statistics.add(mapPlayerStatistics(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve player statistics by result and team ID.", e);
            throw new DataAccessException("Unable to retrieve player statistics by result and team ID.", e);
        }

        return statistics;
    }

    @Override
    public Optional<PlayerStatistics> findByResultIdAndTeamIdAndPlayerId(UUID resultId, UUID teamId, UUID playerId) {
        String query = PLAYER_STATISTICS_SELECT + "WHERE resultId = ? AND teamId = ? AND playerId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, resultId.toString());
            ps.setString(2, teamId.toString());
            ps.setString(3, playerId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPlayerStatistics(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve player statistics by result, team and player ID.", e);
            throw new DataAccessException("Unable to retrieve player statistics by result, team and player ID.", e);
        }

        return Optional.empty();
    }

    @Override
    public PlayerStatistics save(PlayerStatistics playerStatistics) {
        UUID statId = playerStatistics.getStatId() != null ? playerStatistics.getStatId() : UUID.randomUUID();
        String query = "INSERT INTO playerStatistics (statId, resultId, teamId, playerId, tries, assists, tackles, missedTackles, conversions, penalties, metersGained, yellowCards, redCards, statisticDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, statId.toString());
            ps.setString(2, playerStatistics.getResultId().toString());
            ps.setString(3, playerStatistics.getTeamId().toString());
            ps.setString(4, playerStatistics.getPlayerId().toString());
            ps.setInt(5, playerStatistics.getTries());
            ps.setInt(6, playerStatistics.getAssists());
            ps.setInt(7, playerStatistics.getTackles());
            ps.setInt(8, playerStatistics.getMissedTackles());
            ps.setInt(9, playerStatistics.getConversions());
            ps.setInt(10, playerStatistics.getPenalties());
            ps.setInt(11, playerStatistics.getMetersGained());
            ps.setInt(12, playerStatistics.getYellowCards());
            ps.setInt(13, playerStatistics.getRedCards());

            if (playerStatistics.getStatisticDate() != null) {
                ps.setTimestamp(14, Timestamp.valueOf(playerStatistics.getStatisticDate()));
            } else {
                ps.setTimestamp(14, null);
            }

            if (ps.executeUpdate() == 1) {
                playerStatistics.setStatId(statId);
                return playerStatistics;
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to save player statistics.", e);
            throw new DataAccessException("Unable to save player statistics.", e);
        }

        throw new DataAccessException("Unable to save player statistics.", null);
    }
}