package com.vzap.trytons.resource;

import com.vzap.trytons.Annotations.Authenticated;
import com.vzap.trytons.dto.*;
import com.vzap.trytons.exceptions.ApplicationException;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.AdminUserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/admin/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated

public class AdminUserResource {

    private static final Logger LOGGER = Logger.getLogger(AdminUserResource.class.getName());

    @Inject
    AdminUserService adminUserService;

    @GET
    public Response searchUsers(
            @QueryParam("searchTerm") String searchTerm,
            @Context ContainerRequestContext requestContext) {
        try {
            UUID actorUserId = currentUserId(requestContext);

            List<AdminUserSearchResponseDTO> results = adminUserService.searchUsers(actorUserId, searchTerm);

            ApiResponseDTO<List<AdminUserSearchResponseDTO>> payload =
                    ApiResponseDTO.success("User search results retrieved successfully.", results);

            return Response.ok(payload).build();

        } catch (ApplicationException e) {
            return handledApplicationError(e);

        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @PUT
    @Path("{targetUserId}/status")
    @Valid
    public Response updateUserStatus(
            @PathParam("targetUserId") UUID targetUserId, AdminUserStatusRequestDTO request,
            @Context ContainerRequestContext requestContext) {
        try {
            UUID actorUserId = currentUserId(requestContext);
            AdminUserStatusResponseDTO result = adminUserService.updateUserStatus(actorUserId, targetUserId, request);

            ApiResponseDTO<AdminUserStatusResponseDTO> payload =
                    ApiResponseDTO.success("Search executed successfully.", result);

            return Response.ok(payload).build();

        } catch (ApplicationException e) {
            return handledApplicationError(e);

        } catch (Exception e) {
            return unexpected(e);
        }
    }

    //getting methods from Timmy Timmy Timmy - the goat

    private UUID currentUserId(ContainerRequestContext requestContext) {
        Object currentUser = requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);

        if (!(currentUser instanceof AuthPrincipal principal) || principal.getUserId() == null) {
            throw new AuthenticationException("Authentication required");
        }

        return principal.getUserId();
    }

    private Response handledApplicationError(ApplicationException e) {
        ErrorResponseDTO errorPayload = ErrorResponseDTO.of(e.getMessage(), e.getErrorCode());

        return Response.status(e.getStatusCode())
                .entity(errorPayload)
                .build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in AdminUserResource.", e);

        ErrorResponseDTO errorPayload =
                ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorPayload)
                .build();
    }



}
