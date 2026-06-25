package com.vzap.trytons.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
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