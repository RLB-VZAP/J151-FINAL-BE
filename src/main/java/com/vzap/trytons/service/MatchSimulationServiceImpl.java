package com.vzap.trytons.service;

import com.vzap.trytons.dto.MatchResultResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class MatchSimulationServiceImpl implements MatchSimulationService {
    @Override
    public MatchResultResponseDTO simulateFixture(UUID actorUserId, UUID fixtureId) {
        throw new UnsupportedOperationException("MatchSimulationServiceImpl.simulateFixture is a stub.");
    }
}
