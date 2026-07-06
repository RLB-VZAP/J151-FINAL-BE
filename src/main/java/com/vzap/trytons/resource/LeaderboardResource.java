package com.vzap.trytons.resource;

import com.vzap.trytons.service.LeaderboardService;
import com.vzap.trytons.service.PositionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.logging.Logger;

@ApplicationPath("/api")
@Path("/leader-board")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeaderboardResource {
    private Logger LOGGER = Logger.getLogger(PositionResource.class.getName());

    @Inject
    private LeaderboardService leaderboardService;
}
