package com.vzap.trytons.dto.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartLeagueResponseDTO {
    private UUID leagueId;
    private UUID tournamentId;
    private String season;

    private int managerCount;
    private int poolCount;
    private int poolMatchdays;
    private int bracketSize;
    private int totalMatchdays;

    private int fixturesGenerated;

    private String message;
}
