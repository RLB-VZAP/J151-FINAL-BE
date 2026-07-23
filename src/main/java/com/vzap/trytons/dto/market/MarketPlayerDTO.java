package com.vzap.trytons.dto.market;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One row in a market-dashboard panel. Panels populate the fields relevant to
 * them (e.g. captains use captainCount, gems/overpriced use pointsPerValue).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketPlayerDTO {
    private UUID playerId;
    private String playerName;

    private BigDecimal value;

    private int transfersIn;
    private int transfersOut;
    private int netTransfers;

    private int ownershipCount;
    private int recentPoints;
    private BigDecimal pointsPerValue;

    private int captainCount;
}
