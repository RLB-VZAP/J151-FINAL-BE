package com.vzap.trytons.dto;

import com.vzap.trytons.model.Club;
import com.vzap.trytons.model.Position;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class PlayerResponseDTO {
    private UUID playerId;
    private String playerName;
    private BigDecimal value;
    private int attackingAbility;
    private int defensiveAbility;
    private int kickingAbility;
    private int discipline;
    private int consistency;
    private int fitness;
    private int currentForm;
    private int totalFantasyPoints;
    private boolean isActive;
    private Club club;
    private Position position;
    private PlayerResponseDTO player;
    private boolean isCaptain;
    private boolean isViceCaptain;
    private boolean isBench;

}
