package com.vzap.trytons.model;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder

public class Leaderboard {
    private UUID leaderboardId;
    private UUID leagueId;
    private LocalDate lastUpdated;
    private String season;
    private boolean is_master_leaderboard;
}
