package com.vzap.trytons.dao.pricing;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.pricing.PricingSettings;
import jakarta.ejb.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class PricingSettingsDAOImpl extends BaseDAO implements PricingSettingsDAO {

    private static final Logger LOG = Logger.getLogger(PricingSettingsDAOImpl.class.getName());

    private static PricingSettings mapRow(ResultSet rs) {
        try {
            return PricingSettings.builder()
                    .settingsId(UUID.fromString(rs.getString("settingsId")))
                    .weightForm(rs.getBigDecimal("w_form"))
                    .weightPopularity(rs.getBigDecimal("w_popularity"))
                    .weightPoints(rs.getBigDecimal("w_points"))
                    .weightInjury(rs.getBigDecimal("w_injury"))
                    .weightDemand(rs.getBigDecimal("w_demand"))
                    .weightAvailability(rs.getBigDecimal("w_availability"))
                    .maxDeltaPct(rs.getBigDecimal("max_delta_pct"))
                    .minValue(rs.getBigDecimal("min_value"))
                    .maxValue(rs.getBigDecimal("max_value"))
                    .updatedAt(rs.getTimestamp("updatedAt").toLocalDateTime())
                    .build();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<PricingSettings> findSettings() {
        String query = "SELECT * FROM pricing_settings ORDER BY updatedAt DESC LIMIT 1";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to load pricing settings", e);
            throw new DataAccessException("Unable to load pricing settings", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean updateSettings(PricingSettings settings) {
        String query = "UPDATE pricing_settings SET "
                + "w_form = ?, w_popularity = ?, w_points = ?, w_injury = ?, w_demand = ?, w_availability = ?, "
                + "max_delta_pct = ?, min_value = ?, max_value = ? "
                + "WHERE settingsId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setBigDecimal(1, settings.getWeightForm());
            ps.setBigDecimal(2, settings.getWeightPopularity());
            ps.setBigDecimal(3, settings.getWeightPoints());
            ps.setBigDecimal(4, settings.getWeightInjury());
            ps.setBigDecimal(5, settings.getWeightDemand());
            ps.setBigDecimal(6, settings.getWeightAvailability());
            ps.setBigDecimal(7, settings.getMaxDeltaPct());
            ps.setBigDecimal(8, settings.getMinValue());
            ps.setBigDecimal(9, settings.getMaxValue());
            ps.setString(10, settings.getSettingsId().toString());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update pricing settings", e);
            throw new DataAccessException("Unable to update pricing settings", e);
        }
    }

    @Override
    public boolean insertSettings(PricingSettings settings) {
        String query = "INSERT INTO pricing_settings "
                + "(settingsId, w_form, w_popularity, w_points, w_injury, w_demand, w_availability, "
                + "max_delta_pct, min_value, max_value) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, settings.getSettingsId().toString());
            ps.setBigDecimal(2, settings.getWeightForm());
            ps.setBigDecimal(3, settings.getWeightPopularity());
            ps.setBigDecimal(4, settings.getWeightPoints());
            ps.setBigDecimal(5, settings.getWeightInjury());
            ps.setBigDecimal(6, settings.getWeightDemand());
            ps.setBigDecimal(7, settings.getWeightAvailability());
            ps.setBigDecimal(8, settings.getMaxDeltaPct());
            ps.setBigDecimal(9, settings.getMinValue());
            ps.setBigDecimal(10, settings.getMaxValue());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to insert pricing settings", e);
            throw new DataAccessException("Unable to insert pricing settings", e);
        }
    }
}
