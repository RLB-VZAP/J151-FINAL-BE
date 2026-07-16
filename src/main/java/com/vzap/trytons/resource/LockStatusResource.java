package com.vzap.trytons.resource;

import com.vzap.trytons.service.DeadlineLockService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApplicationPath("/api")
@Path("/lock-status")
@Produces(MediaType.APPLICATION_JSON)
//This is a stub
public class LockStatusResource {
    @Inject
    private DeadlineLockService deadlineLockService;

    @GET
    @Path("{fixtureId}")
    public Response getLockStatus(@PathParam("fixtureId") UUID fixtureId) {
        Map<String,String>response = new HashMap<>();
        response.put("message","lock status endpoint is a stub");
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity(response).build();
    }

    @GET
    @Path("/deadline/{fixtureId}")
    public Response getDeadlineStatus(@PathParam("fixtureId") UUID fixtureId) {
        Map<String,String>response = new HashMap<>();
        response.put("message","deadline status endpoint is a stub");
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity(response).build();
    }

}
