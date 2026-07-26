package com.vzap.trytons.dao.device;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.enums.DevicePlatform;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.device.DeviceToken;
import jakarta.ejb.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class DeviceTokenDAOImpl extends BaseDAO implements DeviceTokenDAO {

    private static final Logger LOG = Logger.getLogger(DeviceTokenDAOImpl.class.getName());

    private static DeviceToken mapRow(ResultSet rs) {
        try {
            DeviceToken token = new DeviceToken();
            token.setTokenId(UUID.fromString(rs.getString("tokenId")));
            token.setUserId(UUID.fromString(rs.getString("userId")));
            token.setToken(rs.getString("token"));
            token.setPlatform(DevicePlatform.valueOf(rs.getString("platform")));
            token.setIsActive(rs.getBoolean("isActive"));
            token.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
            token.setLastSeenAt(rs.getTimestamp("last_seen_at").toLocalDateTime());
            return token;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public DeviceToken upsert(DeviceToken token) {
        UUID newId = UUID.randomUUID();
        String query = "INSERT INTO device_token (tokenId, userId, token, platform, isActive) "
                + "VALUES (?, ?, ?, ?, TRUE) "
                + "ON DUPLICATE KEY UPDATE userId = VALUES(userId), platform = VALUES(platform), "
                + "isActive = TRUE, last_seen_at = CURRENT_TIMESTAMP";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, newId.toString());
            ps.setString(2, token.getUserId().toString());
            ps.setString(3, token.getToken());
            ps.setString(4, token.getPlatform().name());
            ps.executeUpdate();

            token.setTokenId(newId);
            token.setIsActive(true);
            return token;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to register device token", e);
            throw new DataAccessException("Unable to register device token", e);
        }
    }

    @Override
    public List<DeviceToken> findActiveByUserId(UUID userId) {
        String query = "SELECT * FROM device_token WHERE userId = ? AND isActive = TRUE";

        List<DeviceToken> tokens = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, userId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tokens.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to load device tokens for user", e);
            throw new DataAccessException("Unable to load device tokens for user", e);
        }
        return tokens;
    }

    @Override
    public List<DeviceToken> findActiveByUserIds(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }

        String placeholders = userIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String query = "SELECT * FROM device_token WHERE isActive = TRUE AND userId IN (" + placeholders + ")";

        List<DeviceToken> tokens = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            int index = 1;
            for (UUID id : userIds) {
                ps.setString(index++, id.toString());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tokens.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to load device tokens for users", e);
            throw new DataAccessException("Unable to load device tokens for users", e);
        }
        return tokens;
    }

    @Override
    public boolean deactivate(String token) {
        String query = "UPDATE device_token SET isActive = FALSE WHERE token = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, token);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to deactivate device token", e);
            throw new DataAccessException("Unable to deactivate device token", e);
        }
    }
}
