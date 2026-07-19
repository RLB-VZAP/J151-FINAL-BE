package com.vzap.trytons.dto.transfer;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TransferRecommendationRequestDTO {
    private UUID teamId;
    private UUID currentPlayerId;
}
