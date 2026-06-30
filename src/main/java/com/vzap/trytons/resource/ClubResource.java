package com.vzap.trytons.resource;

import com.vzap.trytons.dto.ErrorResponse;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.Club;
import com.vzap.trytons.service.ClubService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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



    private Response serverError(String message, DataAccessException e) {
        LOGGER.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponse.of(message, e.getErrorCode())).build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in ClubResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponse.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }
}
