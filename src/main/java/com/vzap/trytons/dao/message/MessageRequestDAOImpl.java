package com.vzap.trytons.dao.message;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.enums.MessageRequestStatus;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.message.MessageRequest;
import jakarta.ejb.Singleton;

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

@Singleton
public class MessageRequestDAOImpl extends BaseDAO implements MessageRequestDAO {

    private static final Logger LOG = Logger.getLogger(MessageRequestDAOImpl.class.getName());

    private static MessageRequest mapRow(ResultSet rs) {
        try {
            MessageRequest request = new MessageRequest();
            request.setRequestId(UUID.fromString(rs.getString("requestId")));
            request.setRequesterUserId(UUID.fromString(rs.getString("requester_user_id")));
            request.setTargetUserId(UUID.fromString(rs.getString("target_user_id")));
            request.setStatus(MessageRequestStatus.valueOf(rs.getString("status")));
            request.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
            Timestamp respondedAt = rs.getTimestamp("respondedAt");
            request.setRespondedAt(respondedAt != null ? respondedAt.toLocalDateTime() : null);
            return request;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public MessageRequest create(MessageRequest request) {
        UUID newId = UUID.randomUUID();
        String query = "INSERT INTO message_request "
                + "(requestId, requester_user_id, target_user_id, status) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, newId.toString());
            ps.setString(2, request.getRequesterUserId().toString());
            ps.setString(3, request.getTargetUserId().toString());
            ps.setString(4, request.getStatus().name());

            if (!(ps.executeUpdate() > 0)) {
                throw new SQLException("No row inserted for message request");
            }

            request.setRequestId(newId);
            return request;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to create message request", e);
            throw new DataAccessException("Unable to create message request", e);
        }
    }

    @Override
    public Optional<MessageRequest> findById(UUID requestId) {
        String query = "SELECT * FROM message_request WHERE requestId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, requestId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find message request by ID", e);
            throw new DataAccessException("Unable to find message request by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<MessageRequest> findByPair(UUID requesterUserId, UUID targetUserId) {
        String query = "SELECT * FROM message_request WHERE requester_user_id = ? AND target_user_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, requesterUserId.toString());
            ps.setString(2, targetUserId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find message request by pair", e);
            throw new DataAccessException("Unable to find message request by pair", e);
        }
        return Optional.empty();
    }

    @Override
    public List<MessageRequest> findIncoming(UUID targetUserId) {
        String query = "SELECT * FROM message_request WHERE target_user_id = ? ORDER BY createdAt DESC";
        return findByUserColumn(query, targetUserId);
    }

    @Override
    public List<MessageRequest> findOutgoing(UUID requesterUserId) {
        String query = "SELECT * FROM message_request WHERE requester_user_id = ? ORDER BY createdAt DESC";
        return findByUserColumn(query, requesterUserId);
    }

    private List<MessageRequest> findByUserColumn(String query, UUID userId) {
        List<MessageRequest> requests = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, userId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to load message requests", e);
            throw new DataAccessException("Unable to load message requests", e);
        }
        return requests;
    }

    @Override
    public boolean updateStatus(UUID requestId, MessageRequestStatus status, LocalDateTime respondedAt) {
        String query = "UPDATE message_request SET status = ?, respondedAt = ? WHERE requestId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, status.name());
            ps.setTimestamp(2, respondedAt != null ? Timestamp.valueOf(respondedAt) : null);
            ps.setString(3, requestId.toString());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update message request status", e);
            throw new DataAccessException("Unable to update message request status", e);
        }
    }

    @Override
    public boolean reopenAsPending(UUID requestId) {
        String query = "UPDATE message_request "
                + "SET status = 'PENDING', respondedAt = NULL, createdAt = CURRENT_TIMESTAMP "
                + "WHERE requestId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, requestId.toString());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to reopen message request", e);
            throw new DataAccessException("Unable to reopen message request", e);
        }
    }

    @Override
    public boolean existsApprovedEitherDirection(UUID userA, UUID userB) {
        String query = "SELECT 1 FROM message_request "
                + "WHERE status = 'APPROVED' "
                + "AND ((requester_user_id = ? AND target_user_id = ?) "
                + "  OR (requester_user_id = ? AND target_user_id = ?)) "
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
            LOG.log(Level.SEVERE, "Unable to check approved message request", e);
            throw new DataAccessException("Unable to check approved message request", e);
        }
    }
}
