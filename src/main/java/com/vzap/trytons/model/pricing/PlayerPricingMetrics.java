package com.vzap.trytons.model.pricing;

import com.vzap.trytons.enums.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Raw per-player signals gathered for a pricing run, before normalization.
 * ownershipCount = teams selecting the player; netTransferDemand = confirmed
 * transfers in minus out; recentFantasyPoints = final points earned.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PlayerPricingMetrics {
    private UUID playerId;
    private String playerName;

    private BigDecimal currentValue;
    private int currentForm;

    private int ownershipCount;
    private int netTransferDemand;
    private int recentFantasyPoints;

    private AvailabilityStatus availabilityStatus;
}
