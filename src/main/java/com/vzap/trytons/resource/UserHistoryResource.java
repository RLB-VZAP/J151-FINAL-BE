package com.vzap.trytons.resource;

import com.vzap.trytons.Annotations.Authenticated;
import com.vzap.trytons.dto.ErrorResponseDTO;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.UserHistoryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/history")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated

public class UserHistoryResource {

    private static final Logger LOGGER = Logger.getLogger(UserHistoryResource.class.getName());

    @Inject
    private UserHistoryService userHistoryService;

    @Context ContainerRequestContext requestContext;


    @GET
    public Response getUserPointsHistory() {
        try {
            UUID requestingUserId = ((AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY)).getUserId();
            return Response.ok(userHistoryService.getUserPointsHistory(requestingUserId.toString())).build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), "NOT_FOUND")).build();
        } catch (AuthorisationException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(ErrorResponseDTO.of(e.getMessage(), "FORBIDDEN")).build();
        } catch (DataAccessException e) {
            return serverError("Failed to load user points history", e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    //Credit goes to Jaunte Kelvin Garcia for making these....SHOUTOUT!
    private Response serverError(String message, DataAccessException e) {
        LOGGER.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponseDTO.of(message, e.getErrorCode())).build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in UserHistoryResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }




}