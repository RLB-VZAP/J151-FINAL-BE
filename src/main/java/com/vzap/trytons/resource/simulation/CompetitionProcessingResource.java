package com.vzap.trytons.resource.simulation;

import com.vzap.trytons.service.simulation.CompetitionProcessingService;
import jakarta.inject.Inject;

// TODO: This resource has no documented REST surface yet (no @Path or HTTP method annotations). Design and
// TODO: add the endpoint(s) needed to trigger CompetitionProcessingService.processDueWork externally for
// TODO: E2E-10 (Match Simulation and Competition Processing), then register this resource in
// TODO: RestApplication - it is not currently registered and is unreachable via HTTP.
public class CompetitionProcessingResource {

    @Inject
    private CompetitionProcessingService competitionProcessingService;
}