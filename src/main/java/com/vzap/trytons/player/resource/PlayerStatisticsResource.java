package com.vzap.trytons.player.resource;

import com.vzap.trytons.player.dto.PlayerStatisticsRequestDTO;
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
@Path("/player-statistics")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlayerStatisticsResource {

    @POST
    public Response captureStatistic(PlayerStatisticsRequestDTO request) {

        throw new UnsupportedOperationException("PlayerStatisticsResource.captureStatistic is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after PlayerStatisticsServiceImpl validation, mapping, and authenticated actor extraction are confirmed.");
    }

    @GET
    @Path("/result/{resultId}")
    public Response listResultStatistics(@PathParam("resultId") UUID resultId) {

        throw new UnsupportedOperationException("PlayerStatisticsResource.listResultStatistics is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after PlayerStatisticsServiceImpl result-based lookup and response mapping are confirmed.");
    }

    @GET
    @Path("/result/{resultId}/team/{teamId}")
    public Response listResultStatisticsForTeam(@PathParam("resultId") UUID resultId, @PathParam("teamId") UUID teamId) {

        throw new UnsupportedOperationException("PlayerStatisticsResource.listResultStatisticsForTeam is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after PlayerStatisticsServiceImpl team-based result lookup and response mapping are confirmed.");
    }
}