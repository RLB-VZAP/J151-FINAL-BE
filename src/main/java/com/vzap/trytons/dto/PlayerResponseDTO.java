package com.vzap.trytons.dto;

import com.vzap.trytons.model.Club;
import com.vzap.trytons.model.Position;

import java.math.BigDecimal;
import java.util.UUID;

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
}
