package com.vzap.trytons.dto.results;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultRequestDTO {
    private UUID fixtureId;
    private int teamAScore;
    private int teamBScore;
}