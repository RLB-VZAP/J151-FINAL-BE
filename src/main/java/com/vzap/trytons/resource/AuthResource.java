package com.vzap.trytons.resource;

import com.vzap.trytons.Annotations.Authenticated;
import com.vzap.trytons.dto.AuthStatusResponseDTO;
import com.vzap.trytons.dto.LoginRequestDTO;
import com.vzap.trytons.dto.LoginResponseDTO;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    private AuthService authService;

    @POST
    @Path("/login")
    public Response login(LoginRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Login request is required.");
        }

        LoginResponseDTO response = authService.authenticate(
                request.getIdentifier(),
                request.getPassword()
        );
        return Response.ok(response).build();
    }

    @POST
    @Path("/logout")
    @Authenticated
    public Response logout() {
        return Response.ok(authService.logout()).build();
    }

    @GET
    @Path("/status")
    public Response getAuthStatus(@QueryParam("requestingUserId") String requestingUserId) {
        AuthStatusResponseDTO response = authService.getAuthStatus(requestingUserId);
        return Response.ok(response).build();
    }
}
