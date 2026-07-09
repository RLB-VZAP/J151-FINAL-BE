package com.vzap.trytons.scoring.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FantasyPointBreakdown {
    private UUID breakdownId;
    private UUID pointsId;

    private String category;
    private int points;
    private String description;
}