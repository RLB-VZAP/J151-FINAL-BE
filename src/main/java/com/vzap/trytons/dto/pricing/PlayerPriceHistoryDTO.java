package com.vzap.trytons.dto.pricing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerPriceHistoryDTO {
    private BigDecimal oldValue;
    private BigDecimal newValue;
    private BigDecimal delta;
    private String reason;
    private LocalDateTime createdAt;
}
