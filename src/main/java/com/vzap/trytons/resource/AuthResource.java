package com.vzap.trytons.resource;

import com.vzap.trytons.dto.AuthApiResponse;
import com.vzap.trytons.dto.ErrorResponse;
import com.vzap.trytons.dto.LoginRequest;
import com.vzap.trytons.dto.LoginResponse;
import com.vzap.trytons.exceptions.ApplicationException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger LOGGER = Logger.getLogger(AuthResource.class.getName());

    @Inject
    private AuthService authService;

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        try {
            if (request == null) {
                throw new ValidationException("Login request is required.");
            }

            LoginResponse loginResponse = authService.authenticate(
                    request.getIdentifier(),
                    request.getPassword()
            );

            AuthApiResponse<LoginResponse> successPayload = AuthApiResponse.success("Login successful.", loginResponse);

            return Response.ok(successPayload).build();

        } catch (ApplicationException e) {
            ErrorResponse handledError = ErrorResponse.of(e.getMessage(), e.getErrorCode());

            return Response.status(e.getStatusCode())
                    .entity(handledError)
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during user login execution", e);

            ErrorResponse fallbackError = ErrorResponse.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR");

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(fallbackError)
                    .build();
        }
    }
}