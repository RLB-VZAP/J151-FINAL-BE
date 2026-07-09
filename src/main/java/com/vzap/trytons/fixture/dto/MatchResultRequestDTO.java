package com.vzap.trytons.fixture.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultRequestDTO {
    private UUID fixtureId;
    private String simulationReason;
}