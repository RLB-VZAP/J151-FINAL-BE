package com.vzap.trytons.service;

import com.vzap.trytons.dto.CompetitionProcessingSummaryDTO;

import java.util.UUID;

public interface CompetitionProcessingService {
    CompetitionProcessingSummaryDTO processDueWork(UUID actorUserId);
}