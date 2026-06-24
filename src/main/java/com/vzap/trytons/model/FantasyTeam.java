package com.vzap.trytons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FantasyTeam {
    private UUID teamID, ownerUserId;
    private String teamName;
    private double totalTeamValue,remainingBudget;
    private Date creationDate;
    private int totalPoints,weeklyPoints;
    private boolean isValid,isLocked;

}
