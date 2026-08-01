package com.vzap.trytons.dto.results;

import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * A team's fantasy score for one fixture, and how it was made up.
 *
 * <p>{@code playerPoints} is the sum of {@code events}; adding the captain
 * bonus and subtracting the transfer penalty gives {@code totalPoints}, which
 * is the score shown on the fixture.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamPointsBreakdownDTO {
    private UUID teamId;
    private String teamName;

    private int playerPoints;
    private int captainBonus;
    private int transferPenalty;
    private int totalPoints;

    private List<PointsByEventDTO> events;
}
