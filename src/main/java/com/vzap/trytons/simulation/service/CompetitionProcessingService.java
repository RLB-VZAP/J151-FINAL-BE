package com.vzap.trytons.simulation.service;

import com.vzap.trytons.simulation.dto.CompetitionProcessingSummaryDTO;

import java.util.UUID;

public interface CompetitionProcessingService {
    CompetitionProcessingSummaryDTO processDueWork(UUID actorUserId);
}