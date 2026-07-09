package com.vzap.trytons.roster.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViewOwnTeamDTO {
    private UUID teamId;
    private String teamName;
    private BigDecimal totalTeamValue;
    private BigDecimal remainingBudget;
    private LocalDateTime creationDate;
    private int totalPoints;
    private int weeklyPoints;
    private Boolean isValid;
    private Boolean isLocked;
    private String ownerUsername;
    private List<PlayerResponseDTO> players;
}
