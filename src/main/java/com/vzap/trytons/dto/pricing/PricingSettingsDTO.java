package com.vzap.trytons.dto.pricing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingSettingsDTO {
    private BigDecimal weightForm;
    private BigDecimal weightPopularity;
    private BigDecimal weightPoints;
    private BigDecimal weightInjury;
    private BigDecimal weightDemand;
    private BigDecimal weightAvailability;

    private BigDecimal maxDeltaPct;
    private BigDecimal minValue;
    private BigDecimal maxValue;
}
