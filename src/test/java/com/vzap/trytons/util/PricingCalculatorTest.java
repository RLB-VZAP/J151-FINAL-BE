package com.vzap.trytons.util;

import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.model.pricing.PlayerPricingMetrics;
import com.vzap.trytons.model.pricing.PricingSettings;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PricingCalculator is the pure math extracted from PricingServiceImpl.computeNewValue.
 * Both the admin-triggered pricing run and the automatic per-round reprice
 * (invoked from CompetitionProcessingServiceImpl) go through the exact same
 * weighting/capping/clamping, so these tests pin that shared behaviour.
 */
class PricingCalculatorTest {

    private static PricingSettings settings(String wForm, String wPopularity, String wPoints,
                                             String wInjury, String wDemand, String wAvailability,
                                             String maxDeltaPct, String minValue, String maxValue) {
        return PricingSettings.builder()
                .settingsId(UUID.randomUUID())
                .weightForm(new BigDecimal(wForm))
                .weightPopularity(new BigDecimal(wPopularity))
                .weightPoints(new BigDecimal(wPoints))
                .weightInjury(new BigDecimal(wInjury))
                .weightDemand(new BigDecimal(wDemand))
                .weightAvailability(new BigDecimal(wAvailability))
                .maxDeltaPct(new BigDecimal(maxDeltaPct))
                .minValue(new BigDecimal(minValue))
                .maxValue(new BigDecimal(maxValue))
                .build();
    }

    private static PlayerPricingMetrics.PlayerPricingMetricsBuilder metrics(BigDecimal currentValue, int currentForm) {
        return PlayerPricingMetrics.builder()
                .playerId(UUID.randomUUID())
                .playerName("Test Player")
                .currentValue(currentValue)
                .currentForm(currentForm)
                .ownershipCount(0)
                .netTransferDemand(0)
                .recentFantasyPoints(0)
                .availabilityStatus(AvailabilityStatus.ACTIVE);
    }

    @Test
    void neutralMetricsLeavePriceUnchanged() {
        // Schema-default weights, but every signal reads as neutral: form at
        // the 50 midpoint, and zero ownership/points/demand ceilings so the
        // ratio-based signals fall back to their 0.0 branch entirely.
        PricingSettings s = settings("0.1000", "0.0500", "0.1000", "0.1500", "0.0800", "0.2000", "0.1500", "1.00", "300.00");
        PlayerPricingMetrics m = metrics(new BigDecimal("100.00"), 50).build();

        BigDecimal newValue = PricingCalculator.computeNewValue(s, m, 0, 0, 0);

        assertEquals(new BigDecimal("100.00"), newValue);
    }

    @Test
    void deltaIsCappedAtMaxDeltaPct() {
        // Only the form weight is active, driven to its maximum (form=100 ->
        // normalized 1.0), so the raw signal (50% of current value) comfortably
        // exceeds the 10% max_delta_pct cap.
        PricingSettings s = settings("0.5000", "0", "0", "0", "0", "0", "0.1000", "1.00", "1000.00");
        PlayerPricingMetrics m = metrics(new BigDecimal("100.00"), 100).build();

        BigDecimal newValue = PricingCalculator.computeNewValue(s, m, 0, 0, 0);

        // current(100) + maxDelta(100 * 0.10) = 110.00, not 150.00.
        assertEquals(new BigDecimal("110.00"), newValue);
    }

    @Test
    void resultIsClampedToMaxValueBound() {
        // A large max_delta_pct lets the full positive signal through, but
        // maxValue is set below what that signal would otherwise produce.
        PricingSettings s = settings("1.0000", "0", "0", "0", "0", "0", "1.0000", "1.00", "105.00");
        PlayerPricingMetrics m = metrics(new BigDecimal("100.00"), 100).build();

        BigDecimal newValue = PricingCalculator.computeNewValue(s, m, 0, 0, 0);

        assertEquals(new BigDecimal("105.00"), newValue);
    }

    @Test
    void resultIsClampedToMinValueBound() {
        PricingSettings s = settings("1.0000", "0", "0", "0", "0", "0", "1.0000", "60.00", "300.00");
        PlayerPricingMetrics m = metrics(new BigDecimal("100.00"), 0).build();

        BigDecimal newValue = PricingCalculator.computeNewValue(s, m, 0, 0, 0);

        assertEquals(new BigDecimal("60.00"), newValue);
    }

    @Test
    void injuryPenaltyPushesPriceDown() {
        // Only the injury weight is active; every other signal is neutral.
        PricingSettings s = settings("0", "0", "0", "0.2000", "0", "0", "0.5000", "1.00", "300.00");
        PlayerPricingMetrics m = metrics(new BigDecimal("100.00"), 50)
                .availabilityStatus(AvailabilityStatus.INJURED)
                .build();

        BigDecimal newValue = PricingCalculator.computeNewValue(s, m, 0, 0, 0);

        // weighted = -0.20 -> rawDelta = -20.00, within the 50% cap.
        assertEquals(new BigDecimal("80.00"), newValue);
        assertTrue(newValue.compareTo(m.getCurrentValue()) < 0);
    }

    @Test
    void suspendedAvailabilityPenaltyPushesPriceDown() {
        PricingSettings s = settings("0", "0", "0", "0", "0", "0.3000", "0.5000", "1.00", "300.00");
        PlayerPricingMetrics m = metrics(new BigDecimal("100.00"), 50)
                .availabilityStatus(AvailabilityStatus.SUSPENDED)
                .build();

        BigDecimal newValue = PricingCalculator.computeNewValue(s, m, 0, 0, 0);

        assertEquals(new BigDecimal("70.00"), newValue);
        assertTrue(newValue.compareTo(m.getCurrentValue()) < 0);
    }

    @Test
    void unavailableStatusAppliesTheSameAvailabilityPenaltyAsSuspended() {
        PricingSettings s = settings("0", "0", "0", "0", "0", "0.3000", "0.5000", "1.00", "300.00");
        PlayerPricingMetrics m = metrics(new BigDecimal("100.00"), 50)
                .availabilityStatus(AvailabilityStatus.UNAVAILABLE)
                .build();

        BigDecimal newValue = PricingCalculator.computeNewValue(s, m, 0, 0, 0);

        assertEquals(new BigDecimal("70.00"), newValue);
    }
}
