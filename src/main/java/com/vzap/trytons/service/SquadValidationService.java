package com.vzap.trytons.service;

import com.vzap.trytons.dto.SquadValidationResult;

import java.util.List;
import java.util.UUID;

public interface SquadValidationService {

    SquadValidationResult validateNewSquad(List<UUID> selectedPlayerIds);

    SquadValidationResult validateTransfer(UUID teamId, UUID removedPlayerId, UUID addedPlayerId);
}
