package com.vzap.trytons.roster.dto;

import com.vzap.trytons.player.dto.PlayerResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class teamResponseDTO {
    private UUID teamId;
    private String teamName;
    private BigDecimal totalTeamValue;
    private BigDecimal remainingBudget;
    private LocalDateTime creationDate;
    private int totalPoints;
    private int weeklyPoints;
    private Boolean isValid;
    private Boolean isLocked;
    private UUID ownerId;
    private List<PlayerResponseDTO> selectedPlayers;
}