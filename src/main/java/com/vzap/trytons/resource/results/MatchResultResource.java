package com.vzap.trytons.resource.results;

import com.vzap.trytons.Annotations.Authenticated;
import com.vzap.trytons.dto.shared.ErrorResponseDTO;
import com.vzap.trytons.dto.results.MatchResultRequestDTO;
import com.vzap.trytons.dto.results.MatchResultResponseDTO;
import com.vzap.trytons.exceptions.ApplicationException;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.results.MatchResultService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Authenticated
@Path("/match-results")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MatchResultResource {

    private static final Logger LOGGER = Logger.getLogger(MatchResultResource.class.getName());

    @Inject
    private MatchResultService matchResultService;

    @POST
    public Response captureResult(@Valid MatchResultRequestDTO request, @Context ContainerRequestContext requestContext) {
        try {
            UUID actorUserId = currentUserId(requestContext);
            MatchResultResponseDTO result = matchResultService.captureResult(actorUserId, request);

            return Response.ok(result).build();
        } catch (ApplicationException e) {
            return handledApplicationError(e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @GET
    @Path("/fixture/{fixtureId}")
    public Response getResult(@PathParam("fixtureId") UUID fixtureId) {
        try {
            MatchResultResponseDTO result = matchResultService.getResult(fixtureId);

            return Response.ok(result).build();
        } catch (ApplicationException e) {
            return handledApplicationError(e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

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
        LOGGER.log(Level.SEVERE, "Unexpected error in MatchResultResource.", e);

        ErrorResponseDTO errorPayload = ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorPayload)
                .build();
    }
}
