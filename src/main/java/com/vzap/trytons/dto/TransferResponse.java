package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TransferResponse {
    private final UUID transferId;
    private final UUID teamId;
    private final UUID removedPlayerId;
    private final UUID addedPlayerId;
    private final LocalDateTime transferDate;
    private final boolean penaltyApplied;
    private final int penaltyPointAmount;
    private final double newRemainingBudget;
    private final double newTotalTeamValue;
}
