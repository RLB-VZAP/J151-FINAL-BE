package com.vzap.trytons.dao.tournament;

import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.tournament.TournamentSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vzap.trytons.dao.shared.BaseDAO;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TournamentSettingsDAOImpl extends BaseDAO implements TournamentSettingsDAO {
    private static final Logger LOG = Logger.getLogger(TournamentSettingsDAOImpl.class.getName());

    @Override
    public Optional<TournamentSettings> getSettings() {
        String query = "SELECT * FROM tournament_settings LIMIT 1";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(this.mapSettings(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to get tournament settings", e);
            throw new DataAccessException("Unable to get tournament settings", e);
        }
        return Optional.empty();
    }

    @Override
    public TournamentSettings insertSettings(TournamentSettings settings) {
        String query = "INSERT INTO tournament_settings (settingsId, win_points, draw_points, loss_points, attack_bonus_threshold, losing_bonus_margin, third_place_playoff) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, settings.getSettingsId().toString());
            ps.setInt(2, settings.getWinPoints());
            ps.setInt(3, settings.getDrawPoints());
            ps.setInt(4, settings.getLossPoints());
            ps.setInt(5, settings.getAttackBonusThreshold());
            ps.setInt(6, settings.getLosingBonusMargin());
            ps.setBoolean(7, settings.isThirdPlacePlayoff());
            if (ps.executeUpdate() == 1) {
                Optional<TournamentSettings> created = getSettings();
                if (created.isPresent()) {
                    return created.get();
                }
                throw new DataAccessException("Tournament settings were inserted, but cannot be retrieved.", null);
            }
        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The tournament settings could not be created because they conflict with an existing record.");
            }
            LOG.log(Level.SEVERE, "Unable to insert tournament settings", e);
            throw new DataAccessException("Unable to insert tournament settings", e);
        }
        return null;
    }

    @Override
    public boolean updateSettings(TournamentSettings settings) {
        String query = "UPDATE tournament_settings SET win_points = ?, draw_points = ?, loss_points = ?, attack_bonus_threshold = ?, losing_bonus_margin = ?, third_place_playoff = ? WHERE settingsId = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, settings.getWinPoints());
            ps.setInt(2, settings.getDrawPoints());
            ps.setInt(3, settings.getLossPoints());
            ps.setInt(4, settings.getAttackBonusThreshold());
            ps.setInt(5, settings.getLosingBonusMargin());
            ps.setBoolean(6, settings.isThirdPlacePlayoff());
            ps.setString(7, settings.getSettingsId().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The tournament settings could not be updated because they conflict with an existing record.");
            }
            LOG.log(Level.SEVERE, "Unable to update tournament settings", e);
            throw new DataAccessException("Unable to update tournament settings", e);
        }
    }

    private TournamentSettings mapSettings(ResultSet rs) throws SQLException {
        Timestamp updatedAtTimestamp = rs.getTimestamp("updatedAt");
        return TournamentSettings.builder()
                .settingsId(UUID.fromString(rs.getString("settingsId")))
                .winPoints(rs.getInt("win_points"))
                .drawPoints(rs.getInt("draw_points"))
                .lossPoints(rs.getInt("loss_points"))
                .attackBonusThreshold(rs.getInt("attack_bonus_threshold"))
                .losingBonusMargin(rs.getInt("losing_bonus_margin"))
                .thirdPlacePlayoff(rs.getBoolean("third_place_playoff"))
                .updatedAt(updatedAtTimestamp == null ? null : updatedAtTimestamp.toLocalDateTime())
                .build();
    }
}
