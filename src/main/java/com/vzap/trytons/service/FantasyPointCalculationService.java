package com.vzap.trytons.service;

import com.vzap.trytons.dto.FantasyPointCalculationResultDTO;

import java.util.UUID;

public interface FantasyPointCalculationService {
    FantasyPointCalculationResultDTO calculateForFixture(String fixtureId);
}