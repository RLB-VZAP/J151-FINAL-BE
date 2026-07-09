package com.vzap.trytons.fixture.model;

import com.vzap.trytons.fixture.enums.FixtureStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Fixture {
    private UUID fixtureId;
    private League league;
    private UUID roundId;

    private FantasyTeam teamA;
    private FantasyTeam teamB;

    private LocalDate fixtureDate;
    private LocalTime fixtureTime;

    private FixtureStatus status;

    private LocalDateTime simulationDate;
    private LocalDateTime createdAt;

    private MatchResult matchResult;
}