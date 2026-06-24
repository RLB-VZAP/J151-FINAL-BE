package com.vzap.trytons.model;

import com.vzap.trytons.enums.FixtureStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Fixture {

    private UUID fixtureId;
    private LocalDate matchDate;
    private LocalTime matchTime;
    private String venue;
    private FixtureStatus status;
    private Boolean isSimulated;
    private Boolean isLocked;
    private int matchRoundNumber;
    private LocalDateTime lockDeadline;

    private League league;
    private MatchResult matchResult;
    private List<Locking>  lockings = new ArrayList<>();
}