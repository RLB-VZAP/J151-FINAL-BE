package com.vzap.trytons.model;

import com.vzap.trytons.enums.TransferWindowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer {
    private UUID transferId;
    private UUID teamId;
    private UUID removedPlayerId;
    private UUID addedPlayerId;
    private Date transferDate;
    private boolean penaltyApplied,confirmed;
    private int penaltyPoints,roundNumber;
    private TransferWindowStatus TransferWindowStatus;
}
