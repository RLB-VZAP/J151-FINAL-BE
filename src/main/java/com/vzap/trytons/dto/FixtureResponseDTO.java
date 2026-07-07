package com.vzap.trytons.dto;

import com.vzap.trytons.enums.FixtureStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class FixtureResponseDTO {
    //STUB
    private UUID fixtureId;
    private UUID leagueId;

    private UUID homeClubId;

    private UUID awayClubId;

    private LocalDate matchDate;

    private LocalTime matchTime;

    private String venue;

    private FixtureStatus status;

    private boolean locked;

    private LocalDateTime lockDeadline;

    private String message;
}
