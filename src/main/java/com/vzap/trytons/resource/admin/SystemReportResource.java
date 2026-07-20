package com.vzap.trytons.resource.admin;

import com.vzap.trytons.Annotations.AdminOnly;
import com.vzap.trytons.Annotations.Authenticated;
import com.vzap.trytons.dto.admin.SystemReportRequestDTO;
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

import java.util.UUID;

@RequestScoped
@Authenticated
@AdminOnly
@Path("/system-reports")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SystemReportResource {

    @Inject
    private SystemReportService systemReportService;

    @Context
    private ContainerRequestContext requestContext;

    @POST
    public Response generateReport(SystemReportRequestDTO request) {
        // TODO: Resolve the authenticated administrator from the request context, call systemReportService.generateReport with their id and the request, and return the created SystemReportResponseDTO with HTTP 201.
    }

    @GET
    public Response listReports() {
        // TODO: Resolve the authenticated administrator from the request context, call systemReportService.listReports with their id, and return the list of SystemReportResponseDTO with HTTP 200.
    }

    @GET
    @Path("/{reportId}")
    public Response getReportById(@PathParam("reportId") UUID reportId) {
        // TODO: Resolve the authenticated administrator from the request context, call systemReportService.getReportById with their id and the reportId, and return the SystemReportResponseDTO with HTTP 200.
    }
}
