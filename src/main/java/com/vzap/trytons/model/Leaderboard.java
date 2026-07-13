package com.vzap.trytons.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vzap.trytons.enums.LeaderBoardScope;
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
    private LocalDateTime lastUpdated;
    private String season;
    private LeaderBoardScope scope;
}
