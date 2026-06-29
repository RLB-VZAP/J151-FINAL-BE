package com.vzap.trytons.resource;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.logging.Logger;

@ApplicationPath("/api")
@Path("/position")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PositionResource {
    private Logger logger = Logger.getLogger(PositionResource.class.getName());
}
