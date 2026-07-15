package com.vzap.trytons.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class FantasyTeam {
    private UUID teamId;
    private String teamName;
    private BigDecimal remainingBudget;
    private LocalDateTime creationDate;
    private Boolean isValid;
    private UUID owner_user_id;
}