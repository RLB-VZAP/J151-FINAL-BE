package com.vzap.trytons.resource.admin;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.admin.SystemReportRequestDTO;
import com.vzap.trytons.dto.admin.SystemReportResponseDTO;
import com.vzap.trytons.dto.shared.ErrorResponseDTO;
import com.vzap.trytons.exceptions.ApplicationException;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.admin.SystemReportService;
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

@RequestScoped
@Authenticated
@AdminOnly
@Path("/system-reports")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SystemReportResource {
    private static final Logger LOGGER = Logger.getLogger(SystemReportResource.class.getName());

    @Inject
    private SystemReportService systemReportService;

    @Context
    private ContainerRequestContext requestContext;

    @POST
    public Response generateReport(SystemReportRequestDTO request) {
        try {
            UUID actorUserId = currentUserId();
            SystemReportResponseDTO response = systemReportService.generateReport(actorUserId, request);

            return Response.status(Response.Status.CREATED)
                    .entity(response)
                    .build();
        } catch (ApplicationException e) {
            return handledApplicationError(e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @GET
    public Response listReports() {
        try {
            UUID actorUserId = currentUserId();
            List<SystemReportResponseDTO> responseList = systemReportService.listReports(actorUserId);

            return Response.status(Response.Status.OK)
                    .entity(responseList)
                    .build();
        } catch (ApplicationException e) {
            return handledApplicationError(e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @GET
    @Path("/{reportId}")
    public Response getReportById(@PathParam("reportId") UUID reportId) {
        try {
            UUID actorUserId = currentUserId();
            SystemReportResponseDTO response = systemReportService.getReportById(actorUserId, reportId);

            return Response.status(Response.Status.OK)
                    .entity(response)
                    .build();
        } catch (ApplicationException e) {
            return handledApplicationError(e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    private UUID currentUserId() {
        AuthPrincipal principal = (AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        if (principal == null) {
            throw new AuthenticationException("The authenticated user could not be identified.");
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
        LOGGER.log(Level.SEVERE, "Unexpected error in SystemReportResource.", e);

        ErrorResponseDTO errorPayload =
                ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorPayload)
                .build();
    }
}
