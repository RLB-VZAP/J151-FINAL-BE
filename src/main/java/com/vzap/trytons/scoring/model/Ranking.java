package com.vzap.trytons.scoring.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
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
    private int previousRanking;
    private int matchesPlayed, matchesWon, matchesDrawn, matchesLost, pointsFor, pointsAgainst, scoreDifference,leaguePoints, totalFantasyPoints;
    private LocalDateTime updatedAt;
}