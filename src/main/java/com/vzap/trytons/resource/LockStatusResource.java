package com.vzap.trytons.resource;

import com.vzap.trytons.dto.DeadlineStatusResponseDTO;
import com.vzap.trytons.dto.ErrorResponseDTO;
import com.vzap.trytons.dto.LockStatusResponseDTO;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.service.DeadlineLockService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/lock-status")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class LockStatusResource {
    private static final Logger LOG = Logger.getLogger(LockStatusResource.class.getName());
    @Inject
    private DeadlineLockService deadlineLockService;

    @GET
    @Path("/{roundId}")
    public Response getLockStatus(@PathParam("roundId") UUID roundId) {
        try {
            LockStatusResponseDTO response = deadlineLockService.getLockStatus(roundId);
            return Response.ok(response).build();
        }catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        } catch (DataAccessException e) {
            return serverError("Failed get lock status.", e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @GET
    @Path("/deadline/{roundId}")
    public Response getDeadlineStatus(@PathParam("roundId") UUID roundId) {
        try{
            DeadlineStatusResponseDTO response = deadlineLockService.getDeadlineStatus(roundId);;
            return Response.ok(response).build();
        }catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        } catch (DataAccessException e) {
            return serverError("Failed to get deadline status.", e);
        } catch (Exception e) {
            return unexpected(e);
        }

    }

    private Response serverError(String message, DataAccessException e) {
        LOG.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of(message, e.getErrorCode())).build();
    }

    private Response unexpected(Exception e) {
        LOG.log(Level.SEVERE, "Unexpected error in LockStatusResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }

}
