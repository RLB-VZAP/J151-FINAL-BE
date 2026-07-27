package com.vzap.trytons.dao.message;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.enums.MessageScope;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.message.MessageReport;
import jakarta.ejb.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class MessageReportDAOImpl extends BaseDAO implements MessageReportDAO {

    private static final Logger LOG = Logger.getLogger(MessageReportDAOImpl.class.getName());

    @Override
    public MessageReport create(MessageReport report) {
        UUID newId = UUID.randomUUID();
        String query = "INSERT INTO message_report "
                + "(reportId, reporter_user_id, message_id, message_scope, reason) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, newId.toString());
            ps.setString(2, report.getReporterUserId().toString());
            ps.setString(3, report.getMessageId().toString());
            ps.setString(4, report.getMessageScope().name());
            ps.setString(5, report.getReason());

            if (!(ps.executeUpdate() > 0)) {
                throw new SQLException("No row inserted for message report");
            }

            report.setReportId(newId);
            return report;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to create message report", e);
            throw new DataAccessException("Unable to create message report", e);
        }
    }

    @Override
    public boolean existsReportForMessage(MessageScope scope, UUID messageId) {
        String query = "SELECT 1 FROM message_report WHERE message_scope = ? AND message_id = ? LIMIT 1";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, scope.name());
            ps.setString(2, messageId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to check message report existence", e);
            throw new DataAccessException("Unable to check message report existence", e);
        }
    }

    @Override
    public boolean existsReportForLeague(UUID leagueId) {
        String query = "SELECT 1 FROM message_report mr "
                + "JOIN league_message lm ON lm.messageId = mr.message_id "
                + "WHERE mr.message_scope = 'LEAGUE' AND lm.leagueId = ? "
                + "LIMIT 1";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, leagueId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to check league report existence", e);
            throw new DataAccessException("Unable to check league report existence", e);
        }
    }
}
