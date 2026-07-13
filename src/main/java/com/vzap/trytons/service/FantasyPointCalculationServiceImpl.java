package com.vzap.trytons.service;

import com.vzap.trytons.dto.FantasyPointCalculationResultDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class FantasyPointCalculationServiceImpl implements FantasyPointCalculationService {

    @Override
    public FantasyPointCalculationResultDTO calculateForFixture(UUID actorUserId, UUID fixtureId) {
        return new FantasyPointCalculationResultDTO();
    }
}
