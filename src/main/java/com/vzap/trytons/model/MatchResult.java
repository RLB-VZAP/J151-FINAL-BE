package com.vzap.trytons.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

public class MatchResult {
    private UUID resultId;
    private int homeScore;
    private int awayScore;
    private LocalDateTime resultDate;
    private Boolean approved;
    private int simulationRunNumber;

    private Fixture fixture;
    private Administrator approvedByAdmin;
}
