package com.vzap.trytons.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LeaderboardEntryResponseDTO {
    private UUID teamId;
    private String teamName;
    private String owner;
    private int rank;
    private int weeklyPoints;
    private int totalPoints;
    private Integer rankMovement;
}
