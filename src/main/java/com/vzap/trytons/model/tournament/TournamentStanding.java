package com.vzap.trytons.model.tournament;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class TournamentStanding {
    private UUID standingId;
    private UUID tournamentId;
    private UUID poolId;
    private UUID teamId;

    private int played;
    private int won;
    private int drawn;
    private int lost;

    private int pointsFor;
    private int pointsAgainst;
    /** Database generated: pointsFor - pointsAgainst. */
    private int pointsDifference;

    private int attackBonus;
    private int losingBonus;
    private int tournamentPoints;

    private Integer position;
    private boolean qualified;

    private LocalDateTime updatedAt;
}
