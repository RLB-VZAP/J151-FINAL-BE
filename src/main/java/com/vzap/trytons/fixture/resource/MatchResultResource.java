package com.vzap.trytons.fixture.resource;

import com.vzap.trytons.fixture.dto.MatchResultRequestDTO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@RequestScoped
@Path("/match-results")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MatchResultResource {

    @POST
    public Response captureResult(MatchResultRequestDTO request) {

        throw new UnsupportedOperationException("MatchResultResource.captureResult is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after MatchResultServiceImpl orchestration and authenticated actor extraction are confirmed.");
    }

    @GET
    @Path("/fixture/{fixtureId}")
    public Response getResult(@PathParam("fixtureId") UUID fixtureId) {

        throw new UnsupportedOperationException("MatchResultResource.getResult is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after MatchResultServiceImpl result lookup and response mapping are confirmed.");
    }
}