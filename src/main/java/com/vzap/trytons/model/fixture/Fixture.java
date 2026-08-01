package com.vzap.trytons.model.fixture;

import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.enums.TournamentStage;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Fixture {
    private UUID fixtureId;
    private UUID leagueId;
    private UUID roundId;
    private UUID teamAId;
    private UUID teamBId;

    private LocalDate fixtureDate;
    private LocalTime fixtureTime;

    private FixtureStatus status;

    /*
        Tournament wiring. All null on a standalone fixture created directly by
        an administrator; a generated fixture always carries tournamentId and
        stage, and poolId only while it is a pool fixture.
    */
    private UUID tournamentId;
    private UUID poolId;
    private TournamentStage stage;
    private Integer bracketSlot;
    private Integer matchdayNumber;

    private LocalDateTime simulationDate;
    private LocalDateTime createdAt;
}