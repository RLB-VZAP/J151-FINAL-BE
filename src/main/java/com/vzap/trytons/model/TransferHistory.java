package com.vzap.trytons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferHistory {
    private UUID transferHistoryId, transferId, removedPlayerId, addedPlayerId;
    private double oldTeamValue, newTeamValue,oldRemainingBudget, newRemainingBudget;
    private int penaltyPoints;
    private LocalDateTime createdAt;
}
