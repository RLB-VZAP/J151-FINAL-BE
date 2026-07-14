package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultResponseDTO {
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
}