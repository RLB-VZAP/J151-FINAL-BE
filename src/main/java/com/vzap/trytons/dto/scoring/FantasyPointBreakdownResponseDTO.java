package com.vzap.trytons.dto.scoring;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FantasyPointBreakdownResponseDTO {

    private UUID breakdownId;
    private UUID pointsId;
    private String category;
    private int points;
    private String description;
}