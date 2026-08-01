package com.vzap.trytons.dto.tournament;

import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.enums.TournamentStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentFixtureResponseDTO {
    private UUID fixtureId;
    private UUID tournamentId;

    private TournamentStage stage;

    private UUID poolId;
    private String poolName;

    private Integer bracketSlot;
    private Integer matchdayNumber;

    private UUID roundId;
    private Integer roundNumber;

    private UUID teamAId;
    private String teamAName;
    private UUID teamBId;
    private String teamBName;

    private Integer teamAScore;
    private Integer teamBScore;


    private FixtureStatus status;

    private LocalDate fixtureDate;
    private LocalTime fixtureTime;
}
