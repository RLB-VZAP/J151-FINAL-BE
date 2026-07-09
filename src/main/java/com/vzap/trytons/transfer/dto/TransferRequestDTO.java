package com.vzap.trytons.transfer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TransferRequestDTO {
    @NotNull(message = "Team ID is required")
    private UUID teamId;
    @NotNull(message = "Removed player is required")
    private UUID removedPlayerId;
    @NotNull(message="Added player is required")
    private UUID addedPlayerId;

}
