package com.vzap.trytons.dao.message;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.message.ConversationThread;
import com.vzap.trytons.model.message.DirectMessage;
import jakarta.ejb.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class DirectMessageDAOImpl extends BaseDAO implements DirectMessageDAO {

    private static final Logger LOG = Logger.getLogger(DirectMessageDAOImpl.class.getName());

    private static DirectMessage mapRow(ResultSet rs) {
        try {
            DirectMessage message = new DirectMessage();
            message.setMessageId(UUID.fromString(rs.getString("messageId")));
            message.setSenderUserId(UUID.fromString(rs.getString("sender_user_id")));
            message.setRecipientUserId(UUID.fromString(rs.getString("recipient_user_id")));
            message.setBody(rs.getString("body"));
            message.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
            message.setIsRead(rs.getBoolean("isRead"));
            return message;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public DirectMessage create(DirectMessage message) {
        UUID newId = UUID.randomUUID();
        String query = "INSERT INTO direct_message "
                + "(messageId, sender_user_id, recipient_user_id, body, isRead) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, newId.toString());
            ps.setString(2, message.getSenderUserId().toString());
            ps.setString(3, message.getRecipientUserId().toString());
            ps.setString(4, message.getBody());
            ps.setBoolean(5, Boolean.TRUE.equals(message.getIsRead()));

            if (!(ps.executeUpdate() > 0)) {
                throw new SQLException("No row inserted for direct message");
            }

            message.setMessageId(newId);
            return message;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to create direct message", e);
            throw new DataAccessException("Unable to create direct message", e);
        }
    }

    @Override
    public List<DirectMessage> findConversation(UUID userA, UUID userB, LocalDateTime since) {
        String query = "SELECT * FROM direct_message "
                + "WHERE ((sender_user_id = ? AND recipient_user_id = ?) "
                + "    OR (sender_user_id = ? AND recipient_user_id = ?)) "
                + "AND (? IS NULL OR createdAt > ?) "
                + "ORDER BY createdAt ASC";

        List<DirectMessage> messages = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, userA.toString());
            ps.setString(2, userB.toString());
            ps.setString(3, userB.toString());
            ps.setString(4, userA.toString());
            Timestamp sinceTs = (since == null) ? null : Timestamp.valueOf(since);
            if (sinceTs == null) {
                ps.setNull(5, Types.TIMESTAMP);
                ps.setNull(6, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(5, sinceTs);
                ps.setTimestamp(6, sinceTs);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to load conversation", e);
            throw new DataAccessException("Unable to load conversation", e);
        }
        return messages;
    }

    @Override
    public List<ConversationThread> findThreads(UUID userId) {
        String query = "SELECT x.counterpartUserId AS counterpartUserId, "
                + "       u.username AS counterpartUsername, "
                + "       x.body AS lastMessageBody, "
                + "       x.createdAt AS lastMessageAt, "
                + "       (SELECT COUNT(*) FROM direct_message d "
                + "          WHERE d.recipient_user_id = ? "
                + "            AND d.sender_user_id = x.counterpartUserId "
                + "            AND d.isRead = FALSE) AS unreadCount "
                + "FROM ( "
                + "    SELECT CASE WHEN dm.sender_user_id = ? THEN dm.recipient_user_id ELSE dm.sender_user_id END AS counterpartUserId, "
                + "           dm.body, "
                + "           dm.createdAt, "
                + "           ROW_NUMBER() OVER ( "
                + "               PARTITION BY CASE WHEN dm.sender_user_id = ? THEN dm.recipient_user_id ELSE dm.sender_user_id END "
                + "               ORDER BY dm.createdAt DESC, dm.messageId DESC "
                + "           ) AS rn "
                + "    FROM direct_message dm "
                + "    WHERE dm.sender_user_id = ? OR dm.recipient_user_id = ? "
                + ") x "
                + "JOIN `user` u ON u.userId = x.counterpartUserId "
                + "WHERE x.rn = 1 "
                + "ORDER BY x.createdAt DESC";

        List<ConversationThread> threads = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            String me = userId.toString();
            ps.setString(1, me);
            ps.setString(2, me);
            ps.setString(3, me);
            ps.setString(4, me);
            ps.setString(5, me);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    threads.add(ConversationThread.builder()
                            .counterpartUserId(UUID.fromString(rs.getString("counterpartUserId")))
                            .counterpartUsername(rs.getString("counterpartUsername"))
                            .lastMessageBody(rs.getString("lastMessageBody"))
                            .lastMessageAt(rs.getTimestamp("lastMessageAt").toLocalDateTime())
                            .unreadCount(rs.getInt("unreadCount"))
                            .build());
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to load conversation threads", e);
            throw new DataAccessException("Unable to load conversation threads", e);
        }
        return threads;
    }

    @Override
    public int markThreadRead(UUID recipientUserId, UUID counterpartUserId) {
        String query = "UPDATE direct_message SET isRead = TRUE "
                + "WHERE recipient_user_id = ? AND sender_user_id = ? AND isRead = FALSE";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, recipientUserId.toString());
            ps.setString(2, counterpartUserId.toString());
            return ps.executeUpdate();

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to mark conversation as read", e);
            throw new DataAccessException("Unable to mark conversation as read", e);
        }
    }

    @Override
    public int countUnread(UUID userId) {
        String query = "SELECT COUNT(*) AS unreadCount FROM direct_message "
                + "WHERE recipient_user_id = ? AND isRead = FALSE";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, userId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("unreadCount");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to count unread direct messages", e);
            throw new DataAccessException("Unable to count unread direct messages", e);
        }
        return 0;
    }
}
