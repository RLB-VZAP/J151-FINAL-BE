package com.vzap.trytons.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDTO {
    @NotNull(message = "Team ID is required")
    private UUID teamId;
    @NotNull(message = "Fixture ID is required")
    private UUID fixtureId;
    private UUID removedPlayerId;
    private UUID addedPlayerId;

}
