package com.vzap.trytons.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class SimulationSettings {

    private UUID simulationSettingsId;
    private BigDecimal homeAdvantageWeight;
    private BigDecimal clubStrengthWeight;
    private BigDecimal playerFormWeight;
    private BigDecimal teamBalanceWeight;
    private BigDecimal randomVariationWeight;
    private Boolean requireAdminApproval;
    private Boolean allowResimulation;
    private int maxResimulations;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private League league;
}