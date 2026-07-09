package com.vzap.trytons.fixture.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResult {

    private UUID resultId;
    private UUID fixtureId;
    private UUID teamAId;
    private UUID teamBId;

    private int simulationRunNumber;
    private int teamAScore;
    private int teamBScore;

    private String winnerSide;

    private boolean draw;
    private boolean approved;
    private boolean current;

    private LocalDateTime resultDate;
    private LocalDateTime approvedAt;

    private UUID approvedByAdminId;
}