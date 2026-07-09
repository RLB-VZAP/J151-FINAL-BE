package com.vzap.trytons.player.dto;

import com.vzap.trytons.player.model.Club;
import com.vzap.trytons.player.model.Position;
import lombok.Getter;

import java.math.BigDecimal;
@Getter
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
}
