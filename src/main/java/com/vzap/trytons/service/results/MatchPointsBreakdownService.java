package com.vzap.trytons.service.results;

import com.vzap.trytons.dto.results.TeamPointsBreakdownDTO;

import java.util.UUID;

public interface MatchPointsBreakdownService {

    /**
     * How a team's fantasy score for one match result was earned: the points
     * contributed by each kind of event, plus the captain bonus and transfer
     * penalty applied on top.
     *
     * @return the breakdown, or null when the fixture has not been simulated
     */
    TeamPointsBreakdownDTO breakdownFor(UUID resultId, UUID teamId);
}
