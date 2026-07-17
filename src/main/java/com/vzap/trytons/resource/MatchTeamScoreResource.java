package com.vzap.trytons.resource;

import com.vzap.trytons.dto.ErrorResponseDTO;
import com.vzap.trytons.dto.MatchTeamScoreResponseDTO;
import com.vzap.trytons.enums.MatchTeamSide;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.service.MatchTeamScoreService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static jakarta.ws.rs.core.Response.serverError;

@RequestScoped
@Path("/match-team-scores")
@Produces(MediaType.APPLICATION_JSON)
public class MatchTeamScoreResource {

    private static final Logger LOG = Logger.getLogger(MatchTeamScoreResource.class.getName());

    @Inject
    private MatchTeamScoreService matchTeamScoreService;

    @GET
    @Path("/{scoreId}")
    public Response getMatchTeamScoreById(@PathParam("scoreId") UUID scoreId) {

        try {
            MatchTeamScoreResponseDTO score = matchTeamScoreService.getMatchTeamScoreById(scoreId);
            return Response.ok(score).build();
        } catch (ValidationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        } catch (DataAccessException e) {
            return serverError("Failed to get match team score", e); //
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @GET
    @Path("/result/{resultId}")
    public Response listMatchTeamScoresForResult(@PathParam("resultId") UUID resultId) {

        try {
            List<MatchTeamScoreResponseDTO> scores = matchTeamScoreService.listMatchTeamScoresForResult(resultId);
            return Response.ok(scores).build();
        } catch (ValidationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        } catch (DataAccessException e) {
            return serverError("Failed to list match team scores", e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @GET
    @Path("/result/{resultId}/side/{teamSide}")
    public Response getMatchTeamScoreForResultSide(@PathParam("resultId") UUID resultId, @PathParam("teamSide") MatchTeamSide teamSide) {

        try {
            MatchTeamScoreResponseDTO score = matchTeamScoreService.getMatchTeamScoreForResultSide(resultId, teamSide);
            return Response.ok(score).build();
        } catch (ValidationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        } catch (DataAccessException e) {
            return serverError("Failed to get match team score for result side", e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    private Response serverError(String message, DataAccessException e) {
        LOG.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of(message, e.getErrorCode())).build();
    }

    private Response unexpected(Exception e) {
        LOG.log(Level.SEVERE, "Unexpected error in MatchTeamScoreResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }
}