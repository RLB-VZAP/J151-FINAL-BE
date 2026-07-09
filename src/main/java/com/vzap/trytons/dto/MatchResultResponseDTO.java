package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MatchResultResponseDTO {
    private UUID resultId;
    private UUID fixtureId;
    private int homeScore;
    private int awayScore;
    private LocalDateTime resultDate;
    private boolean approved;
    private int simulationRunNumber;
}
