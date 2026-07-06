package com.vzap.trytons.service;

import com.vzap.trytons.dto.SquadValidationResultDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SquadValidationService {
    SquadValidationResultDTO  validateSquad(List<UUID> proposedPlayerIds, BigDecimal maximumSquadValue);
}
