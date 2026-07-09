package com.vzap.trytons.resource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@RequestScoped
@Path("/fantasy-point-breakdowns")
@Produces(MediaType.APPLICATION_JSON)
public class FantasyPointBreakdownResource {

    @GET
    @Path("/{breakdownId}")
    public Response getBreakdownById(@PathParam("breakdownId") UUID breakdownId) {

        throw new UnsupportedOperationException("FantasyPointBreakdownResource.getBreakdownById is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after FantasyPointBreakdownServiceImpl read mapping is confirmed.");
    }

    @GET
    @Path("/points/{pointsId}")
    public Response listBreakdownsForPoints(@PathParam("pointsId") UUID pointsId) {

        throw new UnsupportedOperationException("FantasyPointBreakdownResource.listBreakdownsForPoints is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after FantasyPointBreakdownServiceImpl points-based lookup is confirmed.");
    }
}