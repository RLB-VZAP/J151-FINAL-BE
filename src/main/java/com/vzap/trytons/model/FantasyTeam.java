package com.vzap.trytons.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class FantasyTeam {
    private UUID teamId;
    private String teamName;
    private BigDecimal totalTeamValue;
    private BigDecimal remainingBudget;
    private LocalDateTime creationDate;
    private int totalPoints;
    private int weeklyPoints;
    private Boolean isValid;
    private Boolean isLocked;
    private RegisteredUser owner;
}