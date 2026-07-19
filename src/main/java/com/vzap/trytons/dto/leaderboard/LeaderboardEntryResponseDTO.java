package com.vzap.trytons.dto.leaderboard;

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
    private Integer rankMovement;
    private Integer previousRanking;
    private int matchesPlayed, matchesWon, matchesDrawn, matchesLost, pointsFor, pointsAgainst, scoreDifference,
            leaguePoints, totalFantasyPoints;
}
