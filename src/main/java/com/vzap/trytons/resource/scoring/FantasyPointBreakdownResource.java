package com.vzap.trytons.resource.scoring;

import com.vzap.trytons.dto.shared.ErrorResponseDTO;
import com.vzap.trytons.dto.scoring.FantasyPointBreakdownResponseDTO;
import com.vzap.trytons.exceptions.ApplicationException;
import com.vzap.trytons.service.scoring.FantasyPointBreakdownService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequestScoped
@Path("/fantasy-point-breakdowns")
@Produces(MediaType.APPLICATION_JSON)
public class FantasyPointBreakdownResource {

    private static final Logger LOGGER = Logger.getLogger(FantasyPointBreakdownResource.class.getName());

    @Inject
    FantasyPointBreakdownService fantasyPointBreakdownService;

    @GET
    @Path("/{breakdownId}")
    public Response getBreakdownById(@PathParam("breakdownId") UUID breakdownId) {
        try {
            FantasyPointBreakdownResponseDTO result = fantasyPointBreakdownService.getBreakdownById(breakdownId);

            return Response.ok(result).build();

        } catch (ApplicationException e) {
            return handledApplicationError(e);

        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @GET
    @Path("/points/{pointsId}")
    public Response listBreakdownsForPoints(@PathParam("pointsId") UUID pointsId) {
        try {
            List<FantasyPointBreakdownResponseDTO> results = fantasyPointBreakdownService.listBreakdownsForPoints(pointsId);

            return Response.ok(results).build();

        } catch (ApplicationException e) {
            return handledApplicationError(e);

        } catch (Exception e) {
            return unexpected(e);
        }
    }

    private Response handledApplicationError(ApplicationException e) {
        ErrorResponseDTO errorPayload = ErrorResponseDTO.of(e.getMessage(), e.getErrorCode());

        return Response.status(e.getStatusCode())
                .entity(errorPayload)
                .build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in FantasyPointBreakdownResource.", e);

        ErrorResponseDTO errorPayload =
                ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorPayload)
                .build();
    }
}
