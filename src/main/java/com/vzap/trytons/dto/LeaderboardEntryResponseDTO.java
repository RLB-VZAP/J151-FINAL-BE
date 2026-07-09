package com.vzap.trytons.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LeaderboardEntryResponseDTO {
    private UUID teamId;
    private String teamName;
    private String owner;
    private int rank;
    private int previousRank, matchesPlayed, matchesWon, matchesDrawn, matchesLost, pointsFor, pointsAgainst, scoreDifference,
    leaguePoints, totalFantasyPoints;
}
