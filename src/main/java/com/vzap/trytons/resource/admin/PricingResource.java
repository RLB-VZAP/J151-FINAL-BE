package com.vzap.trytons.resource.admin;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.pricing.PricingRunSummaryDTO;
import com.vzap.trytons.dto.pricing.PricingSettingsDTO;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.pricing.PricingService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@RequestScoped
@Path("/admin/pricing")
@Authenticated
@AdminOnly
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PricingResource {

    @Inject
    private PricingService pricingService;

    @Context
    private ContainerRequestContext request;

    private UUID currentUserId() {
        AuthPrincipal principal = (AuthPrincipal) request.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        return principal.getUserId();
    }

    @GET
    @Path("/settings")
    public Response getSettings() {
        return Response.ok(pricingService.getSettings(currentUserId())).build();
    }

    @PUT
    @Path("/settings")
    public Response updateSettings(PricingSettingsDTO body) {
        return Response.ok(pricingService.updateSettings(currentUserId(), body)).build();
    }

    @GET
    @Path("/preview")
    public Response preview() {
        PricingRunSummaryDTO summary = pricingService.preview(currentUserId());
        return Response.ok(summary).build();
    }

    @POST
    @Path("/run")
    public Response run() {
        PricingRunSummaryDTO summary = pricingService.apply(currentUserId(), "Manual admin run");
        return Response.ok(summary).build();
    }
}
