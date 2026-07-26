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
public class PlayerPriceHistory {
    private UUID historyId;

    private UUID playerId;

    private BigDecimal oldValue;
    private BigDecimal newValue;
    private BigDecimal delta;

    private String reason;

    private LocalDateTime createdAt;
}
