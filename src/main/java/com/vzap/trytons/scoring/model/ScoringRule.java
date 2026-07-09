package com.vzap.trytons.scoring.model;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

public class ScoringRule {
    private UUID ruleId;
    private String eventType;
    private int pointsAwarded;
    private Boolean isDeduction;
    private String description;
    private Boolean isActive;

    private League league;
}
