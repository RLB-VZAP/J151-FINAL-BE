package com.vzap.trytons.resource;

import com.vzap.trytons.Annotations.AdminOnly;
import com.vzap.trytons.Annotations.Authenticated;
import com.vzap.trytons.dto.DeadlineStatusResponseDTO;
import com.vzap.trytons.dto.ErrorResponseDTO;
import com.vzap.trytons.dto.LockStatusResponseDTO;
import com.vzap.trytons.exceptions.*;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.DeadlineLockService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/{lock-status}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class LockStatusResource {
    private static final Logger LOG = Logger.getLogger(LockStatusResource.class.getName());
    @Inject
    private DeadlineLockService deadlineLockService;
    @Context
    private ContainerRequestContext request;
    private UUID getCurrentUserId(){
        AuthPrincipal principal = (AuthPrincipal) request.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        return principal.getUserId();
    }


    @GET
    @Path("{roundId}")
    public Response getLockStatus(@PathParam("roundId") UUID roundId) {
        try {
            LockStatusResponseDTO result = deadlineLockService.getLockStatus(roundId);
            return Response.ok(result).build();
        }catch(ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(DataAccessException e){
            return serverError("Failed to get lock status",e);
        }catch (Exception e){
            return unexpected(e);
        }
    }

    @GET
    @Path("/deadline/{roundId}")
    public Response getDeadlineStatus(@PathParam("roundId") UUID roundId) {
        try{
            DeadlineStatusResponseDTO result = deadlineLockService.getDeadlineStatus(roundId);
            return Response.ok(result).build();
        }catch(ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(DataAccessException e){
            return serverError("Failed to get deadline status",e);
        }catch (Exception e){
            return unexpected(e);
        }
    }

    @POST
    @AdminOnly
    @Path("{roundId}")
    public Response lockRound(@PathParam("roundId") UUID roundId, @QueryParam("reason") String reason) {
        try {
            LockStatusResponseDTO responseDTO = deadlineLockService.lockRound(getCurrentUserId(), roundId, reason);
            return Response.ok(responseDTO).build();
        }catch(AuthorisationException e){
            return Response.status(Response.Status.FORBIDDEN).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(ValidationException e){
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(ConflictException e){
            return Response.status(Response.Status.CONFLICT).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(DataAccessException e){
            return serverError("Failed to lock round",e);
        }catch(Exception e){
            return unexpected(e);
        }
    }

    private Response serverError(String message, DataAccessException e){
        LOG.log(Level.SEVERE,message,e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of(message, e.getErrorCode())).build();
    }
    private Response unexpected(Exception e){
        LOG.log(Level.SEVERE,"Unexpected error in LockStatusResource",e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.internalServerError()).build();
    }

}
