package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SimulationSettingResponseDTO {

    private UUID simulationSettingsId;

    private BigDecimal playerFormWeight;
    private BigDecimal teamBalanceWeight;
    private BigDecimal randomVariationWeight;

    private Boolean requireAdminApproval;
    private Boolean allowResimulation;

    private int maxResimulations;

    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FantasyPointBreakdownResponseDTO {

        private UUID breakdownId;
        private UUID pointsId;
        private String category;
        private int points;
        private String description;
    }
}