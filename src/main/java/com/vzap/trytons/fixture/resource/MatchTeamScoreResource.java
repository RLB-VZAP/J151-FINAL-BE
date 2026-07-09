package com.vzap.trytons.fixture.resource;

import com.vzap.trytons.fixture.enums.MatchTeamSide;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@RequestScoped
@Path("/match-team-scores")
@Produces(MediaType.APPLICATION_JSON)
public class MatchTeamScoreResource {

    @GET
    @Path("/{scoreId}")
    public Response getMatchTeamScoreById(@PathParam("scoreId") UUID scoreId) {

        throw new UnsupportedOperationException("MatchTeamScoreResource.getMatchTeamScoreById is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after MatchTeamScoreServiceImpl read mapping is confirmed.");
    }

    @GET
    @Path("/result/{resultId}")
    public Response listMatchTeamScoresForResult(@PathParam("resultId") UUID resultId) {

        throw new UnsupportedOperationException("MatchTeamScoreResource.listMatchTeamScoresForResult is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after MatchTeamScoreServiceImpl result-based lookup is confirmed.");
    }

    @GET
    @Path("/result/{resultId}/side/{teamSide}")
    public Response getMatchTeamScoreForResultSide(@PathParam("resultId") UUID resultId, @PathParam("teamSide") MatchTeamSide teamSide) {

        throw new UnsupportedOperationException("MatchTeamScoreResource.getMatchTeamScoreForResultSide is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after MatchTeamScoreServiceImpl result-side lookup is confirmed.");
    }
}