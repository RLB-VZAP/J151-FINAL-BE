package com.vzap.trytons.simulation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResimulationRequestDTO {
    private UUID fixtureId;
    private String resimulationReason;
}