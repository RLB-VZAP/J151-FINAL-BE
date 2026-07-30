package com.vzap.trytons.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("isActive")
    private boolean isActive;

    /**
     * Current availability (ACTIVE, INJURED, SUSPENDED, ...) as the enum name.
     * Distinct from {@code isActive}, which only says whether the player is still
     * on a club's roster: a rostered player can be injured and so unpickable. The
     * team-selection pool needs both to show a player and refuse the pick.
     */
    private String availabilityStatus;
}