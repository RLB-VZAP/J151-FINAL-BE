package com.vzap.trytons.fixture.resource;

import com.vzap.trytons.fixture.service.FixtureService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.logging.Logger;

@ApplicationPath("")//empty path
@Path("")//empty
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FixtureResource {
    //STUB
    private static final Logger LOG = Logger.getLogger(FixtureResource.class.getName());
    @Inject
    private FixtureService fixtureService;

}
