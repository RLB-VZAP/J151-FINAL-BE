package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FixtureRequestDTO {
    private UUID leagueId;
    private UUID roundId;
    private UUID teamAId;
    private UUID teamBId;
    private String fixtureStatus;
    private LocalDate fixtureDate;
    private LocalTime fixtureTime;
}
