package com.vzap.trytons.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
