package com.vzap.trytons.dao.message;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.message.UserBlock;
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
public class UserBlockDAOImpl extends BaseDAO implements UserBlockDAO {

    private static final Logger LOG = Logger.getLogger(UserBlockDAOImpl.class.getName());

    private static UserBlock mapRow(ResultSet rs) {
        try {
            UserBlock block = new UserBlock();
            block.setBlockId(UUID.fromString(rs.getString("blockId")));
            block.setBlockerUserId(UUID.fromString(rs.getString("blocker_user_id")));
            block.setBlockedUserId(UUID.fromString(rs.getString("blocked_user_id")));
            block.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
            return block;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public UserBlock create(UserBlock block) {
        UUID newId = UUID.randomUUID();
        String query = "INSERT INTO user_block (blockId, blocker_user_id, blocked_user_id) VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, newId.toString());
            ps.setString(2, block.getBlockerUserId().toString());
            ps.setString(3, block.getBlockedUserId().toString());

            if (!(ps.executeUpdate() > 0)) {
                throw new SQLException("No row inserted for user block");
            }

            block.setBlockId(newId);
            return block;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to create user block", e);
            throw new DataAccessException("Unable to create user block", e);
        }
    }

    @Override
    public boolean delete(UUID blockerUserId, UUID blockedUserId) {
        String query = "DELETE FROM user_block WHERE blocker_user_id = ? AND blocked_user_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, blockerUserId.toString());
            ps.setString(2, blockedUserId.toString());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to delete user block", e);
            throw new DataAccessException("Unable to delete user block", e);
        }
    }

    @Override
    public boolean existsEitherDirection(UUID userA, UUID userB) {
        String query = "SELECT 1 FROM user_block "
                + "WHERE (blocker_user_id = ? AND blocked_user_id = ?) "
                + "   OR (blocker_user_id = ? AND blocked_user_id = ?) "
                + "LIMIT 1";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, userA.toString());
            ps.setString(2, userB.toString());
            ps.setString(3, userB.toString());
            ps.setString(4, userA.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to check user block", e);
            throw new DataAccessException("Unable to check user block", e);
        }
    }

    @Override
    public List<UserBlock> findByBlocker(UUID blockerUserId) {
        String query = "SELECT * FROM user_block WHERE blocker_user_id = ? ORDER BY createdAt DESC";

        List<UserBlock> blocks = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, blockerUserId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    blocks.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to load user blocks", e);
            throw new DataAccessException("Unable to load user blocks", e);
        }
        return blocks;
    }
}
