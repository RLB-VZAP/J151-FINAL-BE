package com.vzap.trytons.simulation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SimulationSettingRequestDTO {
    private BigDecimal playerFormWeight;
    private BigDecimal teamBalanceWeight;
    private BigDecimal randomVariationWeight;

    private Boolean requireAdminApproval;
    private Boolean allowResimulation;

    private int maxResimulations;

    private Boolean isActive;
}