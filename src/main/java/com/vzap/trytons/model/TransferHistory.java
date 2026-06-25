package com.vzap.trytons.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class TransferHistory {

    private UUID transferHistoryId;
    private BigDecimal oldTeamValue;
    private BigDecimal newTeamValue;
    private BigDecimal oldRemainingBudget;
    private BigDecimal newRemainingBudget;
    private int penaltyPoints;
    private LocalDateTime createdAt;

    private Transfer transfer;
    private FantasyTeam fantasyTeam;
    private Player removedPlayer;
    private Player addedPlayer;
}