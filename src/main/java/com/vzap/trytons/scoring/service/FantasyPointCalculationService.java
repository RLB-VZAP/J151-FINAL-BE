package com.vzap.trytons.scoring.service;

import com.vzap.trytons.scoring.dto.FantasyPointCalculationResultDTO;

import java.util.UUID;

public interface FantasyPointCalculationService {
    FantasyPointCalculationResultDTO calculateForFixture(UUID actorUserId, UUID fixtureId);
}