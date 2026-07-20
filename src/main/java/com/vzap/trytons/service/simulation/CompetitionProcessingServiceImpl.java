package com.vzap.trytons.service.simulation;

import com.vzap.trytons.dto.simulation.CompetitionProcessingSummaryDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class CompetitionProcessingServiceImpl implements CompetitionProcessingService {

    @Override
    public CompetitionProcessingSummaryDTO processDueWork(UUID actorUserId) {
        // TODO: Find fixtures and rounds whose scheduled processing is due, run the appropriate simulation/scoring/leaderboard-refresh work for each, and return a CompetitionProcessingSummaryDTO describing what was processed.
    }
}
