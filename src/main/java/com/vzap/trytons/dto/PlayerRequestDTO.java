package com.vzap.trytons.dto;

import com.vzap.trytons.model.Club;
import com.vzap.trytons.model.Position;

import java.math.BigDecimal;
import java.util.UUID;

public class PlayerRequestDTO {
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
    private UUID playerId;
    private boolean isCaptain;
    private boolean isViceCaptain;
    private boolean isBench;


}