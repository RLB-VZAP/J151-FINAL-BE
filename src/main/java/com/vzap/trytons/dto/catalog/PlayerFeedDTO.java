package com.vzap.trytons.dto.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One player as returned by the external live feed
 * ({@code GET /players?refresh=true}).
 * <p>
 * The feed is a flat JSON array of these objects. Note it carries no
 * {@code playerId} and no {@code isActive}, and its {@code clubId}/{@code positionId}
 * are the feed's own UUIDs which do not exist in our catalog - they are resolved
 * to our club/position rows during import. Unknown fields are ignored so a feed
 * schema addition never breaks the import (see the Jackson unknown-properties note).
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerFeedDTO {
    private String clubId;
    private String positionId;
    private String playerName;
    private BigDecimal value;
    private int attackingAbility;
    private int defensiveAbility;
    private int kickingAbility;
    private int discipline;
    private int consistency;
    private int fitness;
    private int currentForm;
}
