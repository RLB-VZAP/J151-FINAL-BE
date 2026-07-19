package com.vzap.trytons.dto.scoring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FantasyPointCalculationResultDTO {

    private String fixtureId;
    private int pointsRowsWritten;
    private int calculationVersion;

}