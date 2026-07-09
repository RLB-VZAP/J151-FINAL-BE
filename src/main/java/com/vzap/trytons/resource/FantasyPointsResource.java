package com.vzap.trytons.resource;

import com.vzap.trytons.dto.FantasyPointsRequestDTO;
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
@Path("/fantasy-points")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FantasyPointsResource {

    @POST
    @Path("/calculate")
    public Response calculateFantasyPoints(FantasyPointsRequestDTO request) {

        throw new UnsupportedOperationException("FantasyPointsResource.calculateFantasyPoints is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after FantasyPointsServiceImpl calculation, versioning, and breakdown creation rules are confirmed.");
    }

    @GET
    @Path("/{pointsId}")
    public Response getFantasyPointsById(@PathParam("pointsId") UUID pointsId) {

        throw new UnsupportedOperationException("FantasyPointsResource.getFantasyPointsById is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after FantasyPointsServiceImpl read mapping is confirmed.");
    }

    @GET
    @Path("/stat/{statId}")
    public Response listFantasyPointsForStat(@PathParam("statId") UUID statId) {

        throw new UnsupportedOperationException("FantasyPointsResource.listFantasyPointsForStat is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after FantasyPointsServiceImpl stat-based lookup is confirmed.");
    }

    @GET
    @Path("/stat/{statId}/final")
    public Response getFinalFantasyPointsForStat(@PathParam("statId") UUID statId) {

        throw new UnsupportedOperationException("FantasyPointsResource.getFinalFantasyPointsForStat is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after final-version lookup rules are confirmed.");
    }
}