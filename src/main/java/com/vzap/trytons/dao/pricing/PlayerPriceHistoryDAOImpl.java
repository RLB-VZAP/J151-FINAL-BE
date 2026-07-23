package com.vzap.trytons.dao.pricing;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.pricing.PlayerPriceHistory;
import jakarta.ejb.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class PlayerPriceHistoryDAOImpl extends BaseDAO implements PlayerPriceHistoryDAO {

    private static final Logger LOG = Logger.getLogger(PlayerPriceHistoryDAOImpl.class.getName());

    private static PlayerPriceHistory mapRow(ResultSet rs) {
        try {
            return PlayerPriceHistory.builder()
                    .historyId(UUID.fromString(rs.getString("historyId")))
                    .playerId(UUID.fromString(rs.getString("playerId")))
                    .oldValue(rs.getBigDecimal("oldValue"))
                    .newValue(rs.getBigDecimal("newValue"))
                    .delta(rs.getBigDecimal("delta"))
                    .reason(rs.getString("reason"))
                    .createdAt(rs.getTimestamp("createdAt").toLocalDateTime())
                    .build();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public PlayerPriceHistory create(PlayerPriceHistory history) {
        UUID newId = UUID.randomUUID();
        String query = "INSERT INTO player_price_history (historyId, playerId, oldValue, newValue, reason) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, newId.toString());
            ps.setString(2, history.getPlayerId().toString());
            ps.setBigDecimal(3, history.getOldValue());
            ps.setBigDecimal(4, history.getNewValue());
            ps.setString(5, history.getReason());

            if (!(ps.executeUpdate() > 0)) {
                throw new SQLException("No row inserted for price history");
            }

            history.setHistoryId(newId);
            return history;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to record price history", e);
            throw new DataAccessException("Unable to record price history", e);
        }
    }

    @Override
    public List<PlayerPriceHistory> findByPlayer(UUID playerId, int limit) {
        int cappedLimit = (limit <= 0 || limit > 100) ? 20 : limit;
        String query = "SELECT * FROM player_price_history WHERE playerId = ? ORDER BY createdAt DESC LIMIT ?";

        List<PlayerPriceHistory> history = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, playerId.toString());
            ps.setInt(2, cappedLimit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to load price history", e);
            throw new DataAccessException("Unable to load price history", e);
        }
        return history;
    }
}
