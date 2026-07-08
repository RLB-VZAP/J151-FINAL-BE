package com.vzap.trytons.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FantasyPoints {

    private UUID pointsId;
    private int pointsEarned;
    private LocalDateTime calculationDate;
    private int matchRoundNumber;
    private int calculationVersion;

    private FantasyTeam fantasyTeam;
    private Player player;
    private Fixture fixture;
    private ScoringRule scoringRule;
}