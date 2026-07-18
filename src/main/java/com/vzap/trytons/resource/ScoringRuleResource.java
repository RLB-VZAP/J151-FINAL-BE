package com.vzap.trytons.resource;

import com.vzap.trytons.Annotations.Authenticated;
import com.vzap.trytons.dto.ApiResponseDTO;
import com.vzap.trytons.dto.ErrorResponseDTO;
import com.vzap.trytons.dto.ScoringRuleRequestDTO;
import com.vzap.trytons.dto.ScoringRuleResponseDTO;
import com.vzap.trytons.exceptions.ApplicationException;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.ScoringRuleService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Authenticated
@Path("/scoring-rules")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ScoringRuleResource {

    private static final Logger LOGGER = Logger.getLogger(ScoringRuleResource.class.getName());

    @Inject
    ScoringRuleService scoringRuleService;

    @GET
    public Response listRules(
            @QueryParam("season") String season,
            @Context ContainerRequestContext requestContext) {
        try {
            UUID actorUserId = currentUserId(requestContext);

            List<ScoringRuleResponseDTO> results = scoringRuleService.listRules(actorUserId, season);

            ApiResponseDTO<List<ScoringRuleResponseDTO>> payload =
                    ApiResponseDTO.success("Scoring rules retrieved successfully.", results);

            return Response.ok(payload).build();

        } catch (ApplicationException e) {
            return handledApplicationError(e);

        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @POST
    public Response saveRule(
            @Valid ScoringRuleRequestDTO request,
            @Context ContainerRequestContext requestContext) {
        try {
            UUID actorUserId = currentUserId(requestContext);

            ScoringRuleResponseDTO result = scoringRuleService.saveRule(actorUserId, request);

            ApiResponseDTO<ScoringRuleResponseDTO> payload =
                    ApiResponseDTO.success("Scoring rule saved successfully.", result);

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
        LOGGER.log(Level.SEVERE, "Unexpected error in ScoringRuleResource.", e);

        ErrorResponseDTO errorPayload =
                ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorPayload)
                .build();
    }
}