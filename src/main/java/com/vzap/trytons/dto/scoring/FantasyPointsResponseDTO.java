package com.vzap.trytons.dto.scoring;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("isFinal")
    private boolean isFinal;
    private LocalDateTime calculatedAt;
}