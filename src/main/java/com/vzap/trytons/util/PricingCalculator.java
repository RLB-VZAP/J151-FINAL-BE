package com.vzap.trytons.util;

import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.model.pricing.PlayerPricingMetrics;
import com.vzap.trytons.model.pricing.PricingSettings;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure math for turning a player's raw pricing metrics into a new price.
 * Extracted from PricingServiceImpl (which still owns fetching metrics,
 * applying/persisting changes, and settings loading) so the weighting,
 * delta-capping and clamping rules can be pinned with unit tests the same
 * way ScoringCalculator pins fantasy point scoring.
 */
public final class PricingCalculator {
    private PricingCalculator() {}

    public static BigDecimal computeNewValue(PricingSettings s, PlayerPricingMetrics m,
                                              int maxOwnership, int maxAbsDemand, int maxPoints) {
        double form = (m.getCurrentForm() - 50) / 50.0;
        double popularity = maxOwnership > 0 ? (2.0 * m.getOwnershipCount() / maxOwnership - 1.0) : 0.0;
        double points = maxPoints > 0 ? (2.0 * m.getRecentFantasyPoints() / maxPoints - 1.0) : 0.0;
        double demand = maxAbsDemand > 0 ? ((double) m.getNetTransferDemand() / maxAbsDemand) : 0.0;
        double injury = m.getAvailabilityStatus() == AvailabilityStatus.INJURED ? -1.0 : 0.0;
        double availability = (m.getAvailabilityStatus() == AvailabilityStatus.SUSPENDED
                || m.getAvailabilityStatus() == AvailabilityStatus.UNAVAILABLE) ? -1.0 : 0.0;

        double weighted = s.getWeightForm().doubleValue() * form
                + s.getWeightPopularity().doubleValue() * popularity
                + s.getWeightPoints().doubleValue() * points
                + s.getWeightDemand().doubleValue() * demand
                + s.getWeightInjury().doubleValue() * injury
                + s.getWeightAvailability().doubleValue() * availability;

        double current = m.getCurrentValue().doubleValue();
        double rawDelta = current * weighted;
        double maxDelta = current * s.getMaxDeltaPct().doubleValue();
        double cappedDelta = Math.max(-maxDelta, Math.min(maxDelta, rawDelta));

        double proposed = current + cappedDelta;
        double clamped = Math.max(s.getMinValue().doubleValue(),
                Math.min(s.getMaxValue().doubleValue(), proposed));

        return BigDecimal.valueOf(clamped).setScale(2, RoundingMode.HALF_UP);
    }
}
