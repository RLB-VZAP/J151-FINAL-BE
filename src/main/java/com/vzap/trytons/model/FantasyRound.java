package com.vzap.trytons.model;

import com.vzap.trytons.enums.FantasyRoundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FantasyRound {
    private UUID roundId;
    private String season;
    private int roundNumber;
    private LocalDateTime openDate;
    private LocalDateTime lockDeadline;
    private LocalDateTime endDate;
    private FantasyRoundStatus status;
}
