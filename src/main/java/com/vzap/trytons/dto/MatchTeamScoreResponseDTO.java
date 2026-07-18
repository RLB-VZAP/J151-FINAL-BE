package com.vzap.trytons.dto;

import com.vzap.trytons.enums.MatchTeamSide;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchTeamScoreResponseDTO {
    private UUID teamScoreId;
    private UUID resultId;
    private UUID teamId;
    private MatchTeamSide teamSide;
    private int playerPoints;
    private int captainBonus;
    private int transferPenalty;
    private int totalScore;
    private LocalDateTime calculatedAt;
}