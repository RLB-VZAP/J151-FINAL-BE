package com.vzap.trytons.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

public class Leaderboard {
    private UUID leaderboardId;
    private LocalDateTime lastUpdated;
    private String season;
    private Boolean isMasterLeaderboard;

    private League league;
    private List<Ranking> rankings = new ArrayList<>();
}
