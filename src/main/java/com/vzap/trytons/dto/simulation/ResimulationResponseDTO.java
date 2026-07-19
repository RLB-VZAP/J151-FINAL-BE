package com.vzap.trytons.dto.simulation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResimulationResponseDTO {
    private UUID previousResultId;
    private UUID newResultId;
    private UUID fixtureId;
    private int simulationRunNumber;
    private boolean current;
    private boolean approved;
    private String resimulationReason;
    private LocalDateTime resimulatedAt;
}