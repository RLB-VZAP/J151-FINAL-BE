package com.vzap.trytons.resource;

import com.vzap.trytons.service.PlayerService;
import com.vzap.trytons.service.PlayerStatisticsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.logging.Logger;

@ApplicationPath("/api")
@Path("/player-statistics")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlayerStatisticsResource {
    private static final Logger LOGGER = Logger.getLogger(UserResources.class.getName());
    @Inject
    private PlayerStatisticsService playerStatisticsService;
}
