package com.vzap.trytons.player.resource;

import com.vzap.trytons.shared.dto.ErrorResponseDTO;
import com.vzap.trytons.player.dto.PositionRequestDTO;
import com.vzap.trytons.player.dto.PositionResponseDTO;
import com.vzap.trytons.shared.exceptions.ConflictException;
import com.vzap.trytons.shared.exceptions.DataAccessException;
import com.vzap.trytons.shared.exceptions.ResourceNotFoundException;
import com.vzap.trytons.player.service.PositionService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationPath("/api")
@Path("/position")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PositionResource {
    private Logger LOGGER = Logger.getLogger(PositionResource.class.getName());

    @Inject
    private PositionService positionService;
    @GET
    public Response listPositions(){
        try{
            return  Response.ok(positionService.getAllPositions()).build();
        }catch(DataAccessException e){
            return serverError("Failed to load positions",e);
        }catch(Exception e){
            return unexpected(e);
        }
    }
    @GET
    @Path("/{id}")
    public Response getPosition(@PathParam("id") UUID id){
        try{
            return  Response.ok(positionService.getPosition(id)).build();
        }catch(ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        }catch(DataAccessException e){
            return serverError("Failed to load position",e);
        }catch(Exception e){
            return unexpected(e);
        }
    }

    @POST
    public Response createPosition(@Valid PositionRequestDTO request, @Context UriInfo uriInfo){
        try{
            PositionResponseDTO created = positionService.createPosition(request);
            URI location = uriInfo.getAbsolutePathBuilder().path(created.getPositionId().toString()).build();
            return Response.created(location).entity(created).build();
        }catch(ConflictException e){
            return Response.status(Response.Status.CONFLICT).entity(ErrorResponseDTO.of(e.getMessage() , e.getErrorCode())).build();
        }catch(DataAccessException e){
            return serverError("Failed to create position",e);
        }catch (Exception e){
            return unexpected(e);
        }
    }

    @PUT
    @Path("/{id}")
    public Response updatePosition(@PathParam("id") UUID id, @Valid PositionRequestDTO request){
        try{
            return Response.ok(positionService.updatePosition(id, request)).build();
        }catch(ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        }catch(ConflictException e){
            return Response.status(Response.Status.CONFLICT).build();
        }catch(DataAccessException e){
            return serverError("Failed to update position",e);
        }catch (Exception e){
            return unexpected(e);
        }
    }

    private Response serverError(String message, DataAccessException e) {
        LOGGER.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of(message, e.getErrorCode())).build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in PositionResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }
}
