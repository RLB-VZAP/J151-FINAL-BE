package com.vzap.trytons.dto.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentStandingResponseDTO {
    private UUID teamId;
    private String teamName;
    private String ownerUsername;

    private int seed;

    private int played;
    private int won;
    private int drawn;
    private int lost;

    private int pointsFor;
    private int pointsAgainst;
    private int pointsDifference;

    private int attackBonus;
    private int losingBonus;
    private int tournamentPoints;

    private Integer position;
    private boolean qualified;
}
