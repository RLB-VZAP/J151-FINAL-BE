package com.vzap.trytons.service;

import com.vzap.trytons.dto.FantasyPointCalculationResultDTO;

public interface FantasyPointCalculationService {
    FantasyPointCalculationResultDTO calculateForFixture(String fixtureId);
}