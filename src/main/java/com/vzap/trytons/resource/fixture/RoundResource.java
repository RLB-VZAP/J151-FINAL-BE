package com.vzap.trytons.resource.fixture;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.shared.ErrorResponseDTO;
import com.vzap.trytons.dto.fixture.RoundResponseDTO;
import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.service.fixture.RoundService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/rounds")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class RoundResource {
    private static final Logger LOGGER = Logger.getLogger(RoundResource.class.getName());

    @Inject
    private RoundService roundService;

    @GET
    public Response listRounds(@QueryParam("status") String status) {
        try {
            if (status != null && !status.isBlank()) {
                FantasyRoundStatus parsed = FantasyRoundStatus.valueOf(status.trim().toUpperCase());
                return Response.ok(roundService.listRoundsByStatus(parsed)).build();
            }
            List<RoundResponseDTO> rounds = roundService.listRounds();
            return Response.ok(rounds).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorResponseDTO.of("Invalid round status: " + status, "BAD_REQUEST")).build();
        } catch (DataAccessException e) {
            return serverError("Failed to load rounds", e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @GET
    @Path("/current-open")
    public Response getCurrentOpenRound() {
        try {
            Optional<RoundResponseDTO> round = roundService.getCurrentOpenRound();
            if (round.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of("No open round is currently active.", "NOT_FOUND")).build();
            }
            return Response.ok(round.get()).build();
        } catch (DataAccessException e) {
            return serverError("Failed to load current open round", e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    private Response serverError(String message, DataAccessException e) {
        LOGGER.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of(message, e.getErrorCode())).build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in RoundResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }
}
