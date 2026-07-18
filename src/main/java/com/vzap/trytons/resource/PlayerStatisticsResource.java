package com.vzap.trytons.resource;

import com.vzap.trytons.Annotations.Authenticated;
import com.vzap.trytons.dto.*;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.PlayerStatisticsService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequestScoped
@Path("/player-statistics")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlayerStatisticsResource {
    @Inject
    private PlayerStatisticsService playerStatisticsService;

    private static final Logger LOG = Logger.getLogger(PlayerStatisticsResource.class.getName());

    @POST
    @Authenticated
    public Response captureStatistic(PlayerStatisticsRequestDTO request, @Context ContainerRequestContext containerRequestContext) {
        try{
            UUID actorUserId = currentUserId(containerRequestContext);
            PlayerStatisticsResponseDTO response = playerStatisticsService.captureStatistic(actorUserId, request);
            ApiResponseDTO<PlayerStatisticsResponseDTO> payload = ApiResponseDTO.success("Statistics captured successfully",response);
            return Response.ok(payload).build();
        }catch(AuthenticationException e) {
            LOG.log(Level.WARNING, "Authentication required");
            return Response.status(Response.Status.UNAUTHORIZED).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        }catch (ConflictException e){
            return Response.status(Response.Status.CONFLICT).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        }catch (DataAccessException e){
            return serverError("Unable to capture statistics for player",e);
        }catch(Exception e){
            return unexpected(e);
        }
    }

    @GET
    @Path("/result/{resultId}")
    public Response listResultStatistics(@PathParam("resultId") UUID resultId) {
        try{
            List<PlayerStatisticsResponseDTO> playersStatistics;
            playersStatistics = playerStatisticsService.listResultStatistics(resultId);
            ApiResponseDTO<List<PlayerStatisticsResponseDTO>> payload =
                    ApiResponseDTO.success("Player statistics list retrieved successfully.", playersStatistics);
            return Response.ok(payload).build();
        }catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        } catch (DataAccessException e) {
            return serverError("Unable to list statistics for player", e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @GET
    @Path("/result/{resultId}/team/{teamId}")
    public Response listResultStatisticsForTeam(@PathParam("resultId") UUID resultId, @PathParam("teamId") UUID teamId) {
    try{
        List<PlayerStatisticsResponseDTO> playersStatistics = playerStatisticsService.listResultStatisticsForTeam(resultId, teamId);
        ApiResponseDTO<List<PlayerStatisticsResponseDTO>>payload = ApiResponseDTO.success("Player statistics list retrieved successfully",playersStatistics);
        return Response.ok(payload).build();
    }catch (ResourceNotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
    } catch (DataAccessException e) {
        return serverError("Unable to list statistics for player", e);
    } catch (Exception e) {
        return unexpected(e);
    }
    }

    private UUID currentUserId(ContainerRequestContext requestContext) {
        Object currentUser = requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        if (!(currentUser instanceof AuthPrincipal principal) || principal.getUserId() == null) {
            throw new AuthenticationException("Authentication required");
        }
        return principal.getUserId();
    }
    private Response serverError(String message, DataAccessException e) {
        LOG.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of(message, e.getErrorCode())).build();
    }

    private Response unexpected(Exception e) {
        LOG.log(Level.SEVERE, "Unexpected error in PlayerStatisticsResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }
}
