package com.vzap.trytons.service.simulation;

import com.vzap.trytons.dto.simulation.CompetitionProcessingSummaryDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class CompetitionProcessingServiceImpl implements CompetitionProcessingService {

    @Override
    public CompetitionProcessingSummaryDTO processDueWork(UUID actorUserId) {
        return new CompetitionProcessingSummaryDTO();
    }
}
