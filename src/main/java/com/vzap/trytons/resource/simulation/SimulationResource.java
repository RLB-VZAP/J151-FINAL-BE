package com.vzap.trytons.resource.simulation;

import com.vzap.trytons.service.simulation.SimulationSettingService;
import jakarta.inject.Inject;

// TODO: This resource has no documented REST surface yet (no @Path or HTTP method annotations exist for
// TODO: it in the specification or process inventory). Design and add the simulation-trigger endpoint(s)
// TODO: this resource is meant to expose for E2E-10 (Match Simulation and Competition Processing) before
// TODO: registering it in RestApplication. Note: the injected SimulationSettingService duplicates the
// TODO: already-implemented SimulationSettingResource - confirm this resource still needs that dependency
// TODO: once its actual responsibility is defined.
public class SimulationResource {
    @Inject
    private SimulationSettingService simulationSettingService;
}
