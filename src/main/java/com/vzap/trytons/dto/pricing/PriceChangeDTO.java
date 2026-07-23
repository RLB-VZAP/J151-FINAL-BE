package com.vzap.trytons.dto.pricing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceChangeDTO {
    private UUID playerId;
    private String playerName;

    private BigDecimal oldValue;
    private BigDecimal newValue;
    private BigDecimal delta;
}
