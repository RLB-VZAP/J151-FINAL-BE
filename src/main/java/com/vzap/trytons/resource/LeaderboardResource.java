package com.vzap.trytons.resource;

import com.vzap.trytons.dto.ErrorResponse;
import com.vzap.trytons.dto.LeaderboardEntryResponseDTO;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.service.LeaderboardService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/leaderboard")
@Produces(MediaType.APPLICATION_JSON)

public class LeaderboardResource {
    private static final Logger LOGGER = Logger.getLogger(LeaderboardResource.class.getName());

    @Inject
    private LeaderboardService leaderboardService;

    @GET
    @Path("/{leagueId}/rankings")
    public Response getLeaderboardForLeague(@PathParam("leagueId") UUID leagueId, @Context HttpServletRequest request) {
        UUID requestingUserId = (UUID) request.getAttribute("userId"); //Waiting for AuthFilter.
        try{
            return Response.ok(leaderboardService.getLeaderboardForLeague(leagueId, requestingUserId)).build();
        }catch(ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponse.of(e.getMessage(), "NOT_FOUND")).build();
        }catch(AuthorisationException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(ErrorResponse.of(e.getMessage(), "FORBIDDEN")).build();
        }catch(DataAccessException e){
            return serverError("Failed to load leaderboard for league " + leagueId, e);
        }catch(Exception e){
            return unexpected(e);
        }

    }

    @GET
    @Path("/team/{teamId}")
    public Response getRankingForTeam(@PathParam("teamId") UUID teamId, @QueryParam("leaderboardId") UUID leaderboardId) {
        try{
            Optional<LeaderboardEntryResponseDTO> result = leaderboardService.getRankingForTeam(teamId, leaderboardId);
            if (result.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(result.get()).build();
        }catch(ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }catch(DataAccessException e){
            return serverError("Failed to load ranking for team " + teamId, e);
        }catch(Exception e){
            return unexpected(e);
        }
    }

    //Credit goes to Jaunte Kelvin Garcia for making these....SHOUTOUT!
    private Response serverError(String message, DataAccessException e) {
        LOGGER.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponse.of(message, e.getErrorCode())).build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in LeaderboardResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponse.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }

}
