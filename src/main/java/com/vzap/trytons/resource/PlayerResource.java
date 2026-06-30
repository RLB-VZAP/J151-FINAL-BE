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

import java.net.URI;
import java.security.spec.ECField;
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

    @GET
    @Path("/{id}")
    public Response getPlayer(@PathParam("id") UUID id) {
        try {
            return Response.ok(playerService.getPlayer(id)).build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponse.of(e.getMessage(), e.getErrorCode())).build();
        } catch (DataAccessException e) {
            return serverError("Failed to load player.", e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @POST
    public Response createPlayer(@Valid PlayerRequestDTO playerRequestDTO, @Context UriInfo uriInfo) {
        try{
            PlayerResponseDTO created = playerService.createPlayer(playerRequestDTO);
            URI location = uriInfo.getAbsolutePathBuilder().path(created.getPlayerId().toString()).build();
            return Response.created(location).entity(created).build();
        }catch(ConflictException e){
            return Response.status(Response.Status.CONFLICT).build();
        }catch(DataAccessException e){
            return serverError("Failed to create player.", e);
        }catch (Exception e) {
            return unexpected(e);
        }
    }

    @PUT
    @Path("{/id}")
    public Response updatePlayer(@PathParam("id") UUID id, @Valid PlayerRequestDTO request){
        try{
            return Response.ok(playerService.updatePlayer(id, request)).build();
        }catch (ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponse.of(e.getMessage(), e.getErrorCode())).build();
    }catch (ConflictException e){
            return Response.status(Response.Status.CONFLICT).entity(ErrorResponse.of(e.getMessage(), e.getErrorCode())).build();
        }catch (DataAccessException e){
            return serverError("Failed to update player.", e);
        }catch (Exception e){
            return unexpected(e);
        }
    }



    private PlayerResponseDTO toResponse(Player player) {
        return new PlayerResponseDTO(
                player.getPlayerId(), player.getPlayerName(), player.getValue(), player.getAttackingAbility(),
                player.getDefensiveAbility(), player.getKickingAbility(), player.getDiscipline(), player.getConsistency(),
                player.getFitness(), player.getCurrentForm(), player.getTotalFantasyPoints(), player.isActive(),
                player.getClub(), player.getPosition());
    }

    private Response serverError(String message, DataAccessException e) {
        LOGGER.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponse.of(message, e.getErrorCode())).build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in PlayerResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponse.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }

}
