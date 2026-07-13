package com.vzap.trytons.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ScoringRule {

    private UUID ruleId;
    private String season;
    private String eventType;
    private int pointsAwarded;
    private Boolean isDeduction;
    private String description;
    private Boolean isActive;
}