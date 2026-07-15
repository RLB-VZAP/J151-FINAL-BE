package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FantasyPointsResponseDTO {

    private UUID pointsId;
    private UUID statId;
    private int totalPoints;
    private int calculationVersion;
    private boolean isFinal;
    private LocalDateTime calculatedAt;
}