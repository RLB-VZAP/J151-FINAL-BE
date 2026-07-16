package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerResponseDTO {

    private UUID playerId;
    private UUID clubId;
    private UUID positionId;

    private String playerName;
    private BigDecimal value;

    private int attackingAbility;
    private int defensiveAbility;
    private int kickingAbility;
    private int discipline;
    private int consistency;
    private int fitness;
    private int currentForm;

    private boolean isActive;
    private boolean isCaptain;
    private boolean isViceCaptain;
    private boolean isBench;
}