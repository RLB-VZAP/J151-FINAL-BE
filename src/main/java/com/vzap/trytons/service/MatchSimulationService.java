package com.vzap.trytons.service;

import com.vzap.trytons.dto.MatchResultResponseDTO;

import java.util.UUID;

public interface MatchSimulationService {
    MatchResultResponseDTO simulateFixture(UUID actorUserId, UUID fixtureId);
}