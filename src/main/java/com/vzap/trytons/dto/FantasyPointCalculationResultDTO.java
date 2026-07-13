package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class FantasyPointCalculationResultDTO {

    private String fixtureId;
    private int pointsRowsWritten;
    private int calculationVersion;

}