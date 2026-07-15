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
    private int simulationRunNumber;
    private int teamAScore;
    private int teamBScore;
    private String winnerSide;
    private boolean isDraw;
    private boolean approved;
    private boolean isCurrent;
    private LocalDateTime resultDate;
    private UUID approvedByAdminUserId;
}