package com.vzap.trytons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FantasyPoints {
    private UUID pointsId, teamId, playerId, fixtureId, ruleId;
    private int pointsEarned, matchRoundNumber, calculationVersion;
    private LocalDateTime calculationDate;
}