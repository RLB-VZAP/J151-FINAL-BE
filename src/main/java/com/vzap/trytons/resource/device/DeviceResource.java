package com.vzap.trytons.resource.device;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.device.RegisterDeviceRequestDTO;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.device.DeviceTokenService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.UUID;

@RequestScoped
@Path("/devices")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DeviceResource {

    @Inject
    private DeviceTokenService deviceTokenService;

    @Context
    private ContainerRequestContext request;

    private UUID currentUserId() {
        AuthPrincipal principal = (AuthPrincipal) request.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        return principal.getUserId();
    }

    @POST
    @Path("/register")
    public Response register(RegisterDeviceRequestDTO body) {
        deviceTokenService.register(currentUserId(), body);
        return Response.status(Response.Status.CREATED)
                .entity(Map.of("status", "registered"))
                .build();
    }
}
