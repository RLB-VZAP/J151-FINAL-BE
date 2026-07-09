package com.vzap.trytons.roster.service;

import com.vzap.trytons.roster.dto.SquadValidationResultDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SquadValidationService {
    SquadValidationResultDTO  validateSquad(List<UUID> proposedPlayerIds);
}
