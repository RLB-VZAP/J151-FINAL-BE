package com.vzap.trytons.resource;

import com.vzap.trytons.dto.*;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.service.PlayerService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/player")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlayerResource {
    private static final Logger LOGGER = Logger.getLogger(PlayerResource.class.getName());
    @Inject
    private PlayerService playerService;

    @GET
    public Response listPlayers(@QueryParam("search") String search, @QueryParam("clubId") UUID clubId, @QueryParam("positionId") UUID positionId ) {
        try {
            List<PlayerResponseDTO> players;
            if (search != null || clubId != null || positionId != null) {
                players = playerService.searchPlayers(search, clubId, positionId);
            }else{
                players = playerService.getAllPlayers();
            }
            ApiResponseDTO<List<PlayerResponseDTO>> payload =
                    ApiResponseDTO.success("Player list retrieved successfully.", players);
            return Response.ok(payload).build();
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
            PlayerResponseDTO player = playerService.getPlayer(id);
            ApiResponseDTO<PlayerResponseDTO> payload =
                    ApiResponseDTO.success("Player retrieved successfully.", player);
            return Response.ok(payload).build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
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
            ApiResponseDTO<PlayerResponseDTO>payload = ApiResponseDTO.success("Player created successfully.", created);
            return Response.created(location).entity(payload).build();
        }catch(ConflictException e){
            return Response.status(Response.Status.CONFLICT).build();
        }catch(DataAccessException e){
            return serverError("Failed to create player.", e);
        }catch (Exception e) {
            return unexpected(e);
        }
    }

    @PUT
    @Path("/{id}")
    public Response updatePlayer(@PathParam("id") UUID id, @Valid PlayerRequestDTO request){
        try{
            PlayerResponseDTO updated = playerService.updatePlayer(id, request);
            ApiResponseDTO<PlayerResponseDTO>payload = ApiResponseDTO.success("Player updated successfully.", updated);
            return Response.ok(payload).build();
        }catch (ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
    }catch (ConflictException e){
            return Response.status(Response.Status.CONFLICT).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        }catch (DataAccessException e){
            return serverError("Failed to update player.", e);
        }catch (Exception e){
            return unexpected(e);
        }
    }



    private Response serverError(String message, DataAccessException e) {
        LOGGER.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of(message, e.getErrorCode())).build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in PlayerResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }

}
