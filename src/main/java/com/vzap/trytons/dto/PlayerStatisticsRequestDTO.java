package com.vzap.trytons.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PlayerStatisticsRequestDTO {
    private UUID fixtureId;
    private UUID playerId;
    private int tries;
    private int assists;
    private int tackles;
    private int missedTackles;
    private int conversions;
    private int penalties;
    private int metersGained;
    private int yellowCards;
    private int redCards;
}
