package com.vzap.trytons.dao.market;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.market.PlayerMarketMetrics;
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
public class MarketDemandDAOImpl extends BaseDAO implements MarketDemandDAO {

    private static final Logger LOG = Logger.getLogger(MarketDemandDAOImpl.class.getName());

    private static final String QUERY =
            "SELECT p.playerId, p.playerName, p.value, "
            + "  (SELECT COUNT(*) FROM transfer t  WHERE t.added_player_id   = p.playerId AND t.status = 'CONFIRMED') AS transfersIn, "
            + "  (SELECT COUNT(*) FROM transfer t2 WHERE t2.removed_player_id = p.playerId AND t2.status = 'CONFIRMED') AS transfersOut, "
            + "  (SELECT COUNT(*) FROM team_player_selection tps WHERE tps.playerId = p.playerId) AS ownershipCount, "
            + "  (SELECT COALESCE(SUM(fp.totalPoints), 0) FROM fantasyPoints fp "
            + "     JOIN playerStatistics ps ON ps.statId = fp.statId "
            + "     WHERE ps.playerId = p.playerId AND fp.isFinal = TRUE) AS recentPoints, "
            + "  (SELECT COUNT(*) FROM team_player_selection tc WHERE tc.playerId = p.playerId AND tc.isCaptain = TRUE) AS captainCount "
            + "FROM player p "
            + "WHERE p.isActive = TRUE";

    private static PlayerMarketMetrics mapRow(ResultSet rs) {
        try {
            return PlayerMarketMetrics.builder()
                    .playerId(UUID.fromString(rs.getString("playerId")))
                    .playerName(rs.getString("playerName"))
                    .value(rs.getBigDecimal("value"))
                    .transfersIn(rs.getInt("transfersIn"))
                    .transfersOut(rs.getInt("transfersOut"))
                    .ownershipCount(rs.getInt("ownershipCount"))
                    .recentPoints(rs.getInt("recentPoints"))
                    .captainCount(rs.getInt("captainCount"))
                    .build();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public List<PlayerMarketMetrics> getPlayerMarketMetrics() {
        List<PlayerMarketMetrics> metrics = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(QUERY);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                metrics.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to gather market demand metrics", e);
            throw new DataAccessException("Unable to gather market demand metrics", e);
        }
        return metrics;
    }
}
