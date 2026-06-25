package com.vzap.trytons.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class PerformanceHistory {

    private UUID performanceHistoryId;
    private String season;
    private int roundNumber;
    private int matchesPlayed;
    private int tries;
    private int assists;
    private int tackles;
    private int fantasyPoints;
    private int formRating;
    private LocalDateTime recordedAt;

    private Player player;
}