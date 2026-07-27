package com.vzap.trytons.dao.message;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.enums.MessageStatus;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.message.LeagueMessage;
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
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class LeagueMessageDAOImpl extends BaseDAO implements LeagueMessageDAO {

    private static final Logger LOG = Logger.getLogger(LeagueMessageDAOImpl.class.getName());

    private static LeagueMessage mapRow(ResultSet rs) {
        try {
            LeagueMessage message = new LeagueMessage();
            message.setMessageId(UUID.fromString(rs.getString("messageId")));
            message.setLeagueId(UUID.fromString(rs.getString("leagueId")));
            message.setSenderUserId(UUID.fromString(rs.getString("sender_user_id")));
            message.setBody(rs.getString("body"));
            message.setStatus(MessageStatus.valueOf(rs.getString("status")));
            message.setFlaggedReason(rs.getString("flagged_reason"));
            String moderatedBy = rs.getString("moderated_by_user_id");
            message.setModeratedByUserId(moderatedBy != null ? UUID.fromString(moderatedBy) : null);
            Timestamp moderatedAt = rs.getTimestamp("moderatedAt");
            message.setModeratedAt(moderatedAt != null ? moderatedAt.toLocalDateTime() : null);
            message.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
            return message;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public LeagueMessage create(LeagueMessage message) {
        UUID newId = UUID.randomUUID();
        String query = "INSERT INTO league_message "
                + "(messageId, leagueId, sender_user_id, body, status, flagged_reason) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, newId.toString());
            ps.setString(2, message.getLeagueId().toString());
            ps.setString(3, message.getSenderUserId().toString());
            ps.setString(4, message.getBody());
            ps.setString(5, message.getStatus().name());
            ps.setString(6, message.getFlaggedReason());

            if (!(ps.executeUpdate() > 0)) {
                throw new SQLException("No row inserted for league message");
            }

            message.setMessageId(newId);
            return message;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to create league message", e);
            throw new DataAccessException("Unable to create league message", e);
        }
    }

    @Override
    public List<LeagueMessage> findApprovedByLeague(UUID leagueId, LocalDateTime since) {
        String query = "SELECT * FROM league_message "
                + "WHERE leagueId = ? AND status = 'APPROVED' "
                + "AND (? IS NULL OR createdAt > ?) "
                + "ORDER BY createdAt ASC";

        List<LeagueMessage> messages = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, leagueId.toString());
            Timestamp sinceTs = (since == null) ? null : Timestamp.valueOf(since);
            if (sinceTs == null) {
                ps.setNull(2, Types.TIMESTAMP);
                ps.setNull(3, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(2, sinceTs);
                ps.setTimestamp(3, sinceTs);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to load league messages", e);
            throw new DataAccessException("Unable to load league messages", e);
        }
        return messages;
    }

    @Override
    public List<LeagueMessage> findByStatus(MessageStatus status) {
        String query = "SELECT * FROM league_message WHERE status = ? ORDER BY createdAt ASC";

        List<LeagueMessage> messages = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to load league messages by status", e);
            throw new DataAccessException("Unable to load league messages by status", e);
        }
        return messages;
    }

    @Override
    public Optional<LeagueMessage> findById(UUID messageId) {
        String query = "SELECT * FROM league_message WHERE messageId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, messageId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find league message by ID", e);
            throw new DataAccessException("Unable to find league message by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean updateStatus(UUID messageId, MessageStatus status, UUID moderatorUserId) {
        String query = "UPDATE league_message "
                + "SET status = ?, moderated_by_user_id = ?, moderatedAt = CURRENT_TIMESTAMP "
                + "WHERE messageId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, status.name());
            ps.setString(2, moderatorUserId != null ? moderatorUserId.toString() : null);
            ps.setString(3, messageId.toString());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update league message status", e);
            throw new DataAccessException("Unable to update league message status", e);
        }
    }

    @Override
    public boolean existsFlaggedMessage(UUID leagueId) {
        String query = "SELECT 1 FROM league_message "
                + "WHERE leagueId = ? AND (flagged_reason IS NOT NULL OR status = 'PENDING_REVIEW') "
                + "LIMIT 1";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, leagueId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to check flagged league messages", e);
            throw new DataAccessException("Unable to check flagged league messages", e);
        }
    }
}
