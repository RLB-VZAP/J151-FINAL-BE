package com.vzap.trytons.resource;

import com.vzap.trytons.dto.ErrorResponse;
import com.vzap.trytons.dto.PlayerRequestDTO;
import com.vzap.trytons.dto.PlayerResponseDTO;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.Player;
import com.vzap.trytons.service.PlayerService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static jakarta.ws.rs.core.Response.serverError;

@ApplicationPath("/api")
@Path("/player")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlayerResource {
    private static final Logger LOGGER = Logger.getLogger(UserResources.class.getName());
    @Inject
    private PlayerService playerService;

    @GET
    public Response listPlayers(@QueryParam("search") String search, @QueryParam("clubId") UUID clubId, @QueryParam("positionId") UUID positionId ) {
        try {
            if (search != null || clubId != null || positionId != null) {
                List<PlayerResponseDTO> body = playerService.search(search, clubId, positionId).stream().map(this::toResponse).toList();
                return Response.ok(body).build();
            }
            return Response.ok(playerService.getAllPlayers()).build();
        }catch(DataAccessException e ){
            return serverError("Failed to load players.", e);
        }catch (Exception e) {
            return unexpected(e);
        }
    }

    private PlayerResponseDTO toResponse(Player p) {
        return new PlayerResponseDTO(
                p.getPlayerId(), p.getPlayerName(), p.getValue(),
                p.getAttackingAbility(), p.getDefensiveAbility(), p.getKickingAbility(),
                p.getDiscipline(), p.getConsistency(), p.getFitness(),
                p.getCurrentForm(), p.getTotalFantasyPoints(), p.isActive(),
                p.getClub(), p.getPosition());
    }

    private Response serverError(String message, DataAccessException e) {
        LOGGER.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponse.of(message, e.getErrorCode())).build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in PlayerResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponse.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }

}
