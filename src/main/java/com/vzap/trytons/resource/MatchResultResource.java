package com.vzap.trytons.resource;

import com.vzap.trytons.service.MatchResultService;
import jakarta.inject.Inject;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@ApplicationPath("/api")
@Path("/match-result")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MatchResultResource {
    @Inject
    private MatchResultService matchResultService;
}
