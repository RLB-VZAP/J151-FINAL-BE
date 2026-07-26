package com.vzap.trytons.model.pricing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PricingSettings {
    private UUID settingsId;

    private BigDecimal weightForm;
    private BigDecimal weightPopularity;
    private BigDecimal weightPoints;
    private BigDecimal weightInjury;
    private BigDecimal weightDemand;
    private BigDecimal weightAvailability;

    private BigDecimal maxDeltaPct;
    private BigDecimal minValue;
    private BigDecimal maxValue;

    private LocalDateTime updatedAt;
}
