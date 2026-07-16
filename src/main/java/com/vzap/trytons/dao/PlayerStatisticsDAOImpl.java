package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.Administrator;
import com.vzap.trytons.model.Fixture;
import com.vzap.trytons.model.Player;
import com.vzap.trytons.model.PlayerStatistics;
import jakarta.inject.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class PlayerStatisticsDAOImpl extends BaseDAO implements PlayerStatisticsDAO {

    private static final Logger LOG =
            Logger.getLogger(PlayerStatisticsDAOImpl.class.getName());

    private static final String PLAYER_STATISTICS_SELECT =
            "SELECT statId, fixtureId, playerId, tries, assists, tackles, missedTackles, conversions, penalties, metersGained, yellowCards, redCards, statisticDate, captured_by_admin_user_id AS capturedByAdminUserId FROM playerStatistics ";

    private PlayerStatistics mapPlayerStatistics(ResultSet rs) throws SQLException {
        Fixture fixture = new Fixture();
        fixture.setFixtureId(UUID.fromString(rs.getString("fixtureId")));

        Player player = new Player();
        player.setPlayerId(UUID.fromString(rs.getString("playerId")));

        PlayerStatistics statistics = new PlayerStatistics();
        statistics.setStatId(UUID.fromString(rs.getString("statId")));
        statistics.setTries(rs.getInt("tries"));
        statistics.setAssists(rs.getInt("assists"));
        statistics.setTackles(rs.getInt("tackles"));
        statistics.setMissedTackles(rs.getInt("missedTackles"));
        statistics.setConversions(rs.getInt("conversions"));
        statistics.setPenalties(rs.getInt("penalties"));
        statistics.setMetersGained(rs.getInt("metersGained"));
        statistics.setYellowCards(rs.getInt("yellowCards"));
        statistics.setRedCards(rs.getInt("redCards"));
        statistics.setStatisticDate(rs.getTimestamp("statisticDate").toLocalDateTime());
        statistics.setFixture(fixture);
        statistics.setPlayer(player);
        String capturedByAdminUserId = rs.getString("capturedByAdminUserId");

        if (capturedByAdminUserId != null) {
            Administrator admin = new Administrator();
            admin.setUserId(UUID.fromString(capturedByAdminUserId));
            statistics.setCapturedByAdmin(admin);
        }

        return statistics;
    }

    @Override
    public List<PlayerStatistics> findByFixtureId(UUID fixtureId) {
        String query = PLAYER_STATISTICS_SELECT + "WHERE fixtureId = ? ORDER BY playerId ASC";

        List<PlayerStatistics> statistics = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, fixtureId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    statistics.add(mapPlayerStatistics(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve player statistics by fixture ID.", e);
            throw new DataAccessException("Unable to retrieve player statistics by fixture ID.", e);
        }

        return statistics;
    }

    @Override
    public Optional<PlayerStatistics> save(PlayerStatistics playerStatistics) {
        UUID statId = playerStatistics.getStatId() != null ? playerStatistics.getStatId() : UUID.randomUUID();
        String query = "INSERT INTO playerStatistics (statId, fixtureId, playerId, tries, assists, tackles, missedTackles, conversions, penalties, metersGained, yellowCards, redCards, captured_by_admin_user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, statId.toString());
            ps.setString(2, playerStatistics.getFixture().getFixtureId().toString());
            ps.setString(3, playerStatistics.getPlayer().getPlayerId().toString());
            ps.setInt(4, playerStatistics.getTries());
            ps.setInt(5, playerStatistics.getAssists());
            ps.setInt(6, playerStatistics.getTackles());
            ps.setInt(7, playerStatistics.getMissedTackles());
            ps.setInt(8, playerStatistics.getConversions());
            ps.setInt(9, playerStatistics.getPenalties());
            ps.setInt(10, playerStatistics.getMetersGained());
            ps.setInt(11, playerStatistics.getYellowCards());
            ps.setInt(12, playerStatistics.getRedCards());

            if (playerStatistics.getCapturedByAdmin() != null
                    && playerStatistics.getCapturedByAdmin().getUserId() != null) {
                ps.setString(13, playerStatistics.getCapturedByAdmin().getUserId().toString());
            } else {
                ps.setString(13, null);
            }

            if (ps.executeUpdate() == 1) {
                return findByFixtureId(playerStatistics.getFixture().getFixtureId()).stream()
                        .filter(stat -> statId.equals(stat.getStatId()))
                        .findFirst();
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to save player statistics.", e);
            throw new DataAccessException("Unable to save player statistics.", e);
        }

        return Optional.empty();
    }
}
