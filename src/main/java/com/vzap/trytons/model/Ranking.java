package com.vzap.trytons.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder

public class Ranking {
    private UUID rankingId;
    private UUID leaderboardId;
    private UUID teamId;
    private int currentRanking;
    private Integer previousRanking;
    private int matchesPlayed, matchesWon, matchesDrawn, matchesLost, pointsFor, pointsAgainst, scoreDifference,leaguePoints, total_fantasy_points;
    private LocalDateTime updatedAt;


    public int getRankMovement() {
        return previousRanking <= 0 ? 0 : previousRanking - currentRanking;
    }
}
