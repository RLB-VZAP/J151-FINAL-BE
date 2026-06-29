package com.vzap.trytons.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TransferRequest {
    @NotNull(message = "Team ID is required")
    private UUID teamId;
    private UUID removedPlayerId;
    private UUID addedPlayerId;

}
