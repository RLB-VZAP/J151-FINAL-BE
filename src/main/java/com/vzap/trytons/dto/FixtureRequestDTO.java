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
    //STUB - please confirm what all is needed with the model!
    private LocalDate matchDate;
    private LocalTime matchTime;
    private String venue;
    private int matchRoundNumber;
    private UUID awayClubId;
    private UUID homeClubId;
    private UUID leagueId;
}
