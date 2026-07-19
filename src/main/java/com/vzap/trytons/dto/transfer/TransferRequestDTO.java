package com.vzap.trytons.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequestDTO {

    @NotBlank(message = "Team ID is required")
    private String teamId;
    @NotBlank(message = "Round ID is required")
    private String roundId;
    @NotBlank(message = "Removed player is required")
    private String removedPlayerId;
    @NotBlank(message = "Added player is required")
    private String addedPlayerId;
    private boolean penaltyConfirmed;
}