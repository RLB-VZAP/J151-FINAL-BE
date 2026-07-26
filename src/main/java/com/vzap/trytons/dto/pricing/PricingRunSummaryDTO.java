package com.vzap.trytons.dto.pricing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingRunSummaryDTO {
    private LocalDateTime runAt;

    private boolean applied;

    private int playersEvaluated;
    private int playersRepriced;

    private BigDecimal totalIncrease;
    private BigDecimal totalDecrease;

    private List<PriceChangeDTO> changes;
}
