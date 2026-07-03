package com.vzap.trytons.resource;

import com.vzap.trytons.dto.ClubRequestDTO;
import com.vzap.trytons.dto.ClubResponseDTO;
import com.vzap.trytons.dto.ErrorResponseDTO;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.service.ClubService;
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
@Path("/club")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ClubResource {
    private static final Logger LOGGER = Logger.getLogger(ClubResource.class.getName());
    @Inject
    private ClubService clubService;

    @GET
    public Response listClubs() {
        try{
            return Response.ok(clubService.getAllClubs()).build();
        }catch(DataAccessException e) {
            return serverError("Failed to load clubs", e);
        }catch(Exception e){
            return unexpected(e);
        }
    }
    @GET
    @Path("/{id}")
    public Response getClubById(@PathParam("id") UUID id) {
        try{
            return Response.ok(clubService.getClub(id)).build();
        }catch(ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }catch(DataAccessException e){
            return serverError("Failed to load club", e);
        }catch(Exception e){
            return unexpected(e);
        }
    }

    @POST
    public Response createClub(@Valid ClubRequestDTO request, @Context UriInfo uriInfo) {
        try {
            ClubResponseDTO created = clubService.createClub(request);
            URI location = uriInfo.getAbsolutePathBuilder().path(created.getClubId().toString()).build();
            return Response.created(location).entity(created).build();
        }catch(ConflictException e){
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }catch(DataAccessException e){
            return serverError("Failed to create club", e);
        }catch(Exception e){
            return unexpected(e);
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateClub(@PathParam("id") UUID id, @Valid ClubRequestDTO request) {
        try{
            return Response.ok(clubService.updatePlayer(id, request)).build();
        }catch(ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }catch(ConflictException e){
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }catch(DataAccessException e){
            return serverError("Failed to update club", e);
        }catch(Exception e){
            return unexpected(e);
        }
    }

    private Response serverError(String message, DataAccessException e) {
        LOGGER.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponseDTO.of(message, e.getErrorCode())).build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in ClubResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }
}
