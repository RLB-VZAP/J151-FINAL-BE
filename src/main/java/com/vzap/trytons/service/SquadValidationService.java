package com.vzap.trytons.service;

import com.vzap.trytons.dto.SquadValidationResultDTO;

import java.util.List;
import java.util.UUID;

public interface SquadValidationService {
    SquadValidationResultDTO validateNewSquad(List<UUID> selectedPlayerIds);
    SquadValidationResultDTO validateTransfer(UUID teamId, UUID removedPlayerId, UUID addedPlayerId);
}
