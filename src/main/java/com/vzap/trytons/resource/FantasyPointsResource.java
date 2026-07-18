package com.vzap.trytons.resource;

import com.vzap.trytons.Annotations.Authenticated;
import com.vzap.trytons.dto.ApiResponseDTO;
import com.vzap.trytons.dto.ErrorResponseDTO;
import com.vzap.trytons.dto.FantasyPointsRequestDTO;
import com.vzap.trytons.dto.FantasyPointsResponseDTO;
import com.vzap.trytons.exceptions.ApplicationException;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.FantasyPointsService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
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

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Authenticated
@Path("/fantasy-points")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FantasyPointsResource {

    private static final Logger LOGGER = Logger.getLogger(FantasyPointsResource.class.getName());

    @Inject
    FantasyPointsService fantasyPointsService;

    @POST
    @Path("/calculate")
    public Response calculateFantasyPoints(
            FantasyPointsRequestDTO request,
            @Context ContainerRequestContext requestContext) {
        try {
            UUID actorUserId = currentUserId(requestContext);

            FantasyPointsResponseDTO result = fantasyPointsService.calculateFantasyPoints(actorUserId, request);

            ApiResponseDTO<FantasyPointsResponseDTO> payload = ApiResponseDTO.success("Fantasy points calculated successfully.", result);

            return Response.ok(payload).build();

        } catch (ApplicationException e) {
            return handledApplicationError(e);

        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @GET
    @Path("/{pointsId}")
    public Response getFantasyPointsById(@PathParam("pointsId") UUID pointsId) {
        try {
            FantasyPointsResponseDTO result = fantasyPointsService.getFantasyPointsById(pointsId);

            ApiResponseDTO<FantasyPointsResponseDTO> payload = ApiResponseDTO.success("Fantasy points retrieved successfully.", result);

            return Response.ok(payload).build();

        } catch (ApplicationException e) {
            return handledApplicationError(e);

        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @GET
    @Path("/stat/{statId}")
    public Response listFantasyPointsForStat(@PathParam("statId") UUID statId) {
        try {
            List<FantasyPointsResponseDTO> results = fantasyPointsService.listFantasyPointsForStat(statId);

            ApiResponseDTO<List<FantasyPointsResponseDTO>> payload = ApiResponseDTO.success("Fantasy points for statistic retrieved successfully.", results);

            return Response.ok(payload).build();

        } catch (ApplicationException e) {
            return handledApplicationError(e);

        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @GET
    @Path("/stat/{statId}/final")
    public Response getFinalFantasyPointsForStat(@PathParam("statId") UUID statId) {
        try {
            FantasyPointsResponseDTO result = fantasyPointsService.getFinalFantasyPointsForStat(statId);

            ApiResponseDTO<FantasyPointsResponseDTO> payload = ApiResponseDTO.success("Final fantasy points retrieved successfully.", result);

            return Response.ok(payload).build();

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
        LOGGER.log(Level.SEVERE, "Unexpected error in FantasyPointsResource.", e);

        ErrorResponseDTO errorPayload =
                ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorPayload)
                .build();
    }
}