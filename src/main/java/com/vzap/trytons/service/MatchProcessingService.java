package com.vzap.trytons.service;

import com.vzap.trytons.dto.MatchProcessingResultDTO;

import java.util.UUID;

public interface MatchProcessingService {
    MatchProcessingResultDTO processCompletedFixture(UUID actorUserId, UUID fixtureId);
}
