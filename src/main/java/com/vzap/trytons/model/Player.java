package com.vzap.trytons.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Player {

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
    private Boolean isActive;

    private Club club;
    private Position position;

    public void setPlayerId(boolean isCaptain) {
    }

    public void setViceCaptain(boolean isViceCaptain) {
    }

    public void setBench(boolean isBench) {
    }

    public void setPlayer(Player playerStub) {
    }
}