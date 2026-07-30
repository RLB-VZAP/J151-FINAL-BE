package com.vzap.trytons.dao.message;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.enums.MessageRequestStatus;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.message.MessageRequest;
import jakarta.ejb.Singleton;

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
public class MessageRequestDAOImpl extends BaseDAO implements MessageRequestDAO {

    private static final Logger LOG = Logger.getLogger(MessageRequestDAOImpl.class.getName());

    /* Every read joins both usernames so callers can render a request without a
       second lookup per row. */
    private static final String SELECT_BASE =
            "SELECT mr.*, "
                    + "       requester.username AS requesterUsername, "
                    + "       addressee.username AS addresseeUsername "
                    + "FROM message_request mr "
                    + "JOIN `user` requester ON requester.userId = mr.requester_user_id "
                    + "JOIN `user` addressee ON addressee.userId = mr.addressee_user_id ";

    private static MessageRequest mapRow(ResultSet rs) {
        try {
            Timestamp respondedAt = rs.getTimestamp("respondedAt");
            return MessageRequest.builder()
                    .requestId(UUID.fromString(rs.getString("requestId")))
                    .requesterUserId(UUID.fromString(rs.getString("requester_user_id")))
                    .addresseeUserId(UUID.fromString(rs.getString("addressee_user_id")))
                    .status(MessageRequestStatus.valueOf(rs.getString("status")))
                    .introMessage(rs.getString("introMessage"))
                    .createdAt(rs.getTimestamp("createdAt").toLocalDateTime())
                    .respondedAt(respondedAt == null ? null : respondedAt.toLocalDateTime())
                    .requesterUsername(rs.getString("requesterUsername"))
                    .addresseeUsername(rs.getString("addresseeUsername"))
                    .build();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public MessageRequest create(MessageRequest request) {
        UUID newId = UUID.randomUUID();
        String query = "INSERT INTO message_request "
                + "(requestId, requester_user_id, addressee_user_id, status, introMessage) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, newId.toString());
            ps.setString(2, request.getRequesterUserId().toString());
            ps.setString(3, request.getAddresseeUserId().toString());
            ps.setString(4, (request.getStatus() == null ? MessageRequestStatus.PENDING : request.getStatus()).name());
            ps.setString(5, request.getIntroMessage());

            if (!(ps.executeUpdate() > 0)) {
                throw new SQLException("No row inserted for message request");
            }

            request.setRequestId(newId);
            return request;

        } catch (SQLException e) {
            // uk_message_request_pair means the caller already asked this user;
            // surfaced as a conflict so the service can reopen instead of failing.
            if (e.getMessage() != null && e.getMessage().contains("uk_message_request_pair")) {
                throw new ConflictException("You have already sent this user a message request.");
            }
            LOG.log(Level.SEVERE, "Unable to create message request", e);
            throw new DataAccessException("Unable to create message request", e);
        }
    }

    @Override
    public Optional<MessageRequest> findById(UUID requestId) {
        return findOne(SELECT_BASE + "WHERE mr.requestId = ?", requestId.toString());
    }

    @Override
    public Optional<MessageRequest> findByPair(UUID requesterUserId, UUID addresseeUserId) {
        return findOne(SELECT_BASE + "WHERE mr.requester_user_id = ? AND mr.addressee_user_id = ?",
                requesterUserId.toString(), addresseeUserId.toString());
    }

    @Override
    public Optional<MessageRequest> findBetween(UUID userA, UUID userB) {
        // ACCEPTED first so a mutual pair of rows cannot hide an existing
        // acceptance behind a stale PENDING or DECLINED one.
        String query = SELECT_BASE
                + "WHERE (mr.requester_user_id = ? AND mr.addressee_user_id = ?) "
                + "   OR (mr.requester_user_id = ? AND mr.addressee_user_id = ?) "
                + "ORDER BY FIELD(mr.status, 'ACCEPTED', 'PENDING', 'DECLINED'), mr.createdAt DESC "
                + "LIMIT 1";
        return findOne(query, userA.toString(), userB.toString(), userB.toString(), userA.toString());
    }

    @Override
    public boolean isAcceptedBetween(UUID userA, UUID userB) {
        String query = "SELECT 1 FROM message_request "
                + "WHERE status = 'ACCEPTED' "
                + "  AND ((requester_user_id = ? AND addressee_user_id = ?) "
                + "   OR  (requester_user_id = ? AND addressee_user_id = ?)) "
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
            LOG.log(Level.SEVERE, "Unable to check message request permission", e);
            throw new DataAccessException("Unable to check message request permission", e);
        }
    }

    @Override
    public List<MessageRequest> findPendingForAddressee(UUID addresseeUserId) {
        return findMany(SELECT_BASE + "WHERE mr.addressee_user_id = ? AND mr.status = 'PENDING' "
                + "ORDER BY mr.createdAt DESC", addresseeUserId.toString());
    }

    @Override
    public List<MessageRequest> findPendingFromRequester(UUID requesterUserId) {
        return findMany(SELECT_BASE + "WHERE mr.requester_user_id = ? AND mr.status = 'PENDING' "
                + "ORDER BY mr.createdAt DESC", requesterUserId.toString());
    }

    @Override
    public List<MessageRequest> findAllInvolving(UUID userId) {
        return findMany(SELECT_BASE + "WHERE mr.requester_user_id = ? OR mr.addressee_user_id = ? "
                + "ORDER BY mr.createdAt DESC", userId.toString(), userId.toString());
    }

    @Override
    public boolean updateStatus(UUID requestId, MessageRequestStatus status) {
        String query = "UPDATE message_request SET status = ?, respondedAt = CURRENT_TIMESTAMP "
                + "WHERE requestId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, status.name());
            ps.setString(2, requestId.toString());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update message request status", e);
            throw new DataAccessException("Unable to update message request status", e);
        }
    }

    @Override
    public boolean reopen(UUID requestId, String introMessage) {
        String query = "UPDATE message_request "
                + "SET status = 'PENDING', introMessage = ?, createdAt = CURRENT_TIMESTAMP, respondedAt = NULL "
                + "WHERE requestId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, introMessage);
            ps.setString(2, requestId.toString());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to reopen message request", e);
            throw new DataAccessException("Unable to reopen message request", e);
        }
    }

    private Optional<MessageRequest> findOne(String query, String... params) {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to load message request", e);
            throw new DataAccessException("Unable to load message request", e);
        }
        return Optional.empty();
    }

    private List<MessageRequest> findMany(String query, String... params) {
        List<MessageRequest> requests = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            bind(ps, params);
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

    private void bind(PreparedStatement ps, String... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setString(i + 1, params[i]);
        }
    }
}
