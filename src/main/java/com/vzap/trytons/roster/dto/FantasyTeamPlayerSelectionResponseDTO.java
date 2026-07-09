package com.vzap.trytons.roster.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class FantasyTeamPlayerSelectionResponseDTO {
    private UUID playerId;
    private String playerName;
    private UUID positionId;
    private String positionName;
    private UUID clubId;
    private String clubName;
    private BigDecimal value;
    private Boolean isActive;
    private Integer totalFantasyPoints;
    private Integer currentForm;
}
