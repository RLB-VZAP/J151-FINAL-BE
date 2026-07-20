package com.vzap.trytons.resource.leaderboard;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.shared.ErrorResponseDTO;
import com.vzap.trytons.dto.leaderboard.LeaderboardEntryResponseDTO;
import com.vzap.trytons.dto.leaderboard.LeaderboardRefreshResultDTO;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.leaderboard.LeaderboardService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/leaderboard")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated

public class LeaderboardResource {
    private static final Logger LOGGER = Logger.getLogger(LeaderboardResource.class.getName());

    @Inject
    private LeaderboardService leaderboardService;

    @Context
    private ContainerRequestContext requestContext;

    @GET
    @Path("/{leagueId}/rankings")
    public Response getLeaderboardForLeague(@PathParam("leagueId") UUID leagueId) {
        UUID requestingUserId = ((AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY)).getUserId();
        try{
            List<LeaderboardEntryResponseDTO> leaderboard = leaderboardService.getLeaderboardForLeague(leagueId, requestingUserId);

            return Response.ok(leaderboard).build();
        }catch(ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), "NOT_FOUND")).build();
        }catch(AuthorisationException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(ErrorResponseDTO.of(e.getMessage(), "FORBIDDEN")).build();
        }catch(DataAccessException e){
            return serverError("Failed to load leaderboard for league " + leagueId, e);
        }catch(Exception e){
            return unexpected(e);
        }
    }

    @GET
    @Path("/team/{teamId}")
    public Response getRankingForTeam(@PathParam("teamId") UUID teamId, @QueryParam("leaderboardId") UUID leaderboardId) {
        UUID requestingUserId = ((AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY)).getUserId();
        try{
            Optional<LeaderboardEntryResponseDTO> result = leaderboardService.getRankingForTeam(teamId, leaderboardId, requestingUserId);
            if (result.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            return Response.ok(result).build();
        }catch(ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), "NOT_FOUND")).build();
        }catch(AuthorisationException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(ErrorResponseDTO.of(e.getMessage(), "FORBIDDEN")).build();
        }catch(DataAccessException e){
            return serverError("Failed to load ranking for team " + teamId, e);
        }catch(Exception e){
            return unexpected(e);
        }
    }

    @POST
    @Path("/{leagueId}/refresh")
    public Response refreshLeagueLeaderboard(@PathParam("leagueId") UUID leagueId) {
        UUID actorUserId = ((AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY)).getUserId();
        try{
            LeaderboardRefreshResultDTO result = leaderboardService.refreshLeagueLeaderboard(actorUserId, leagueId);

            return Response.ok(result).build();
        }catch(ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), "NOT_FOUND")).build();
        }catch(AuthorisationException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(ErrorResponseDTO.of(e.getMessage(), "FORBIDDEN")).build();
        }catch(DataAccessException e){
            return serverError("Failed to refresh leaderboard for league " + leagueId, e);
        }catch(Exception e){
            return unexpected(e);
        }
    }

    @GET
    @Path("/master")
    public Response getOverallLeaderboard(){
        UUID requestingUserId = ((AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY)).getUserId();
        try{
            List<LeaderboardEntryResponseDTO> leaderboard = leaderboardService.getOverallLeaderboard(requestingUserId);

            return Response.ok(leaderboard).build();
        }catch(ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), "NOT_FOUND")).build();
        }catch(AuthorisationException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(ErrorResponseDTO.of(e.getMessage(), "FORBIDDEN")).build();
        }catch(DataAccessException e){
            return serverError("Failed to load overall leaderboard", e);
        }catch(Exception e){
            return unexpected(e);
        }
    }

    @POST
    @Path("/master/refresh")
    public Response refreshMaterLeaderboard() {
        UUID actorUserId = ((AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY)).getUserId();
        try{
            LeaderboardRefreshResultDTO result = leaderboardService.refreshOverallLeaderboard(actorUserId);

            return Response.ok(result).build();
        }catch(ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), "NOT_FOUND")).build();
        }catch(AuthorisationException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(ErrorResponseDTO.of(e.getMessage(), "FORBIDDEN")).build();
        }catch(DataAccessException e){
            return serverError("Failed to refresh overall leaderboard", e);
        }catch(Exception e){
            return unexpected(e);
        }
    }


    //Credit goes to Jaunte Kelvin Garcia for making these....SHOUTOUT!
    private Response serverError(String message, DataAccessException e) {
        LOGGER.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponseDTO.of(message, e.getErrorCode())).build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in LeaderboardResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }

}
