package com.vzap.trytons.model.market;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Raw market signals for a single player, aggregated across all confirmed
 * transfers, current squad selections, and final fantasy points.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PlayerMarketMetrics {
    private UUID playerId;
    private String playerName;

    private BigDecimal value;

    private int transfersIn;
    private int transfersOut;

    private int ownershipCount;
    private int recentPoints;
    private int captainCount;
}
