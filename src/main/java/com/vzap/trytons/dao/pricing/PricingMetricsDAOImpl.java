package com.vzap.trytons.dao.pricing;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.dao.shared.PublicLeagueScope;
import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.pricing.PlayerPricingMetrics;
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
public class PricingMetricsDAOImpl extends BaseDAO implements PricingMetricsDAO {

    private static final Logger LOG = Logger.getLogger(PricingMetricsDAOImpl.class.getName());

    private static final String QUERY =
            "SELECT p.playerId, p.playerName, p.value, p.currentForm, "
            + "  (SELECT COUNT(*) FROM team_player_selection tps WHERE tps.playerId = p.playerId) AS ownershipCount, "
            + "  ((SELECT COUNT(*) FROM transfer t  WHERE t.added_player_id   = p.playerId AND t.status = 'CONFIRMED') "
            + "   - (SELECT COUNT(*) FROM transfer t2 WHERE t2.removed_player_id = p.playerId AND t2.status = 'CONFIRMED')) AS netDemand, "
            + "  (SELECT COALESCE(SUM(fp.totalPoints), 0) FROM fantasyPoints fp "
            + "     JOIN playerStatistics ps ON ps.statId = fp.statId "
            + "     WHERE ps.playerId = p.playerId AND fp.isFinal = TRUE "
            + "       AND " + PublicLeagueScope.PLAYER_STATISTICS_FILTER + ") AS recentPoints, "
            + "  (SELECT pa.status FROM playerAvailability pa WHERE pa.playerId = p.playerId "
            + "     ORDER BY pa.effectiveDate DESC LIMIT 1) AS availabilityStatus "
            + "FROM player p "
            + "WHERE p.isActive = TRUE";

    private static PlayerPricingMetrics mapRow(ResultSet rs) {
        try {
            String status = rs.getString("availabilityStatus");
            AvailabilityStatus availability = (status == null)
                    ? AvailabilityStatus.ACTIVE
                    : AvailabilityStatus.valueOf(status);

            return PlayerPricingMetrics.builder()
                    .playerId(UUID.fromString(rs.getString("playerId")))
                    .playerName(rs.getString("playerName"))
                    .currentValue(rs.getBigDecimal("value"))
                    .currentForm(rs.getInt("currentForm"))
                    .ownershipCount(rs.getInt("ownershipCount"))
                    .netTransferDemand(rs.getInt("netDemand"))
                    .recentFantasyPoints(rs.getInt("recentPoints"))
                    .availabilityStatus(availability)
                    .build();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public List<PlayerPricingMetrics> getMetricsForActivePlayers() {
        List<PlayerPricingMetrics> metrics = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(QUERY);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                metrics.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to gather pricing metrics", e);
            throw new DataAccessException("Unable to gather pricing metrics", e);
        }
        return metrics;
    }
}
