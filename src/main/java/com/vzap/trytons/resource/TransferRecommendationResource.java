package com.vzap.trytons.resource;

import com.vzap.trytons.dto.ErrorResponseDTO;
import com.vzap.trytons.dto.TransferRecommendationRequestDTO;
import com.vzap.trytons.dto.TransferRecommendationResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.service.TransferRecommendationService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.rmi.UnexpectedException;
import java.security.Principal;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static jakarta.ws.rs.core.Response.serverError;

@Path("/transfer-recommendations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransferRecommendationResource {

    private static final Logger LOGGER = Logger.getLogger(TransferRecommendationResource.class.getName());

    @Inject
    private TransferRecommendationService transferRecommendationService;

    @POST
    public Response recommendTransfers(@Valid TransferRecommendationRequestDTO request,
                                       @Context SecurityContext securityContext) {
        try{
            UUID userId = currentUserId(securityContext);
            TransferRecommendationResponseDTO response = transferRecommendationService.recommendTransfers(userId, request);
            return Response.status(Response.Status.OK).entity(response).build();

        }catch(AuthenticationException e){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }catch (AuthorisationException e){
            return Response.status(Response.Status.FORBIDDEN).build();
        }catch (ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).build();
        }catch (DataAccessException e){
            return serverError("Failed to generate transfer recommendations", e);
        }catch (Exception e){
            return unexpected(e);
        }
    }

    private UUID currentUserId(SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();

        if(principal == null || principal.getName() == null) {
            throw new AuthenticationException("Authentication required");
        }

        try{
            return UUID.fromString(principal.getName());
        }catch (IllegalArgumentException e){
            throw new AuthenticationException("Invalid authentication");
        }
    }

    private Response serverError(String message, DataAccessException e) {
        LOGGER.log(Level.SEVERE, message, e);
        return  Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .build();
    }

    private Response unexpected(Exception e){
        LOGGER.log(Level.SEVERE, "Unexpected error found in TransferRecommendationResource", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponseDTO.of("An unexpected error has occured", "INTERNAL_SERVER_ERROR"))
                .build();
    }
}
