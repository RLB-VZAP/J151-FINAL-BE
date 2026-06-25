package com.vzap.trytons.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Ranking {

    private UUID rankingId;
    private int currentRanking;
    private int previousRanking;
    private int weeklyScore;
    private int totalScore;
    private int rankMovement;
    private LocalDateTime updatedAt;

    private Leaderboard leaderboard;
    private FantasyTeam fantasyTeam;
}