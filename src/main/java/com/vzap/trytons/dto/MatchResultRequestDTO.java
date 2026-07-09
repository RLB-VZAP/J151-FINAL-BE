package com.vzap.trytons.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class MatchResultRequestDTO {
    private UUID fixtureId;
    private int homeScore;
    private int awayScore;
    private int simulationRunNumber;
}
