package com.vzap.trytons.resource;

import com.vzap.trytons.Annotations.Authenticated;
import com.vzap.trytons.dto.*;
import com.vzap.trytons.exceptions.ApplicationException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
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
    public Response login(LoginRequestDTO request) {
        try {
            if (request == null) {
                throw new ValidationException("Login request is required.");
            }

            LoginResponseDTO loginResponseDTO = authService.authenticate(
                    request.getIdentifier(),
                    request.getPassword()
            );

            ApiResponseDTO<LoginResponseDTO> successPayload = ApiResponseDTO.success("Login successful.", loginResponseDTO);

            return Response.ok(successPayload).build();

        } catch (ApplicationException e) {
            ErrorResponseDTO handledError = ErrorResponseDTO.of(e.getMessage(), e.getErrorCode());

            return Response.status(e.getStatusCode())
                    .entity(handledError)
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during user login execution", e);

            ErrorResponseDTO fallbackError = ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR");

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(fallbackError)
                    .build();
        }
    }
    @POST
    @Path("/logout")
    @Authenticated
    public Response logout() {
        try {
            String acknowledgement = authService.logout();

            ApiResponseDTO<Void> successPayload =
                    ApiResponseDTO.success(acknowledgement, null);

            return Response.ok(successPayload).build();

        } catch (ApplicationException e) {
            ErrorResponseDTO handledError = ErrorResponseDTO.of(e.getMessage(), e.getErrorCode());
            return Response.status(e.getStatusCode())
                    .entity(handledError)
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during logout acknowledgement", e);
            ErrorResponseDTO fallbackError =
                    ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(fallbackError)
                    .build();
        }
    }

    @GET
    @Path("/status")
    public Response getAuthStatus(@QueryParam("requestingUserId") String requestingUserId) {
        try {
            AuthStatusResponseDTO statusResponse = authService.getAuthStatus(requestingUserId);

            ApiResponseDTO<AuthStatusResponseDTO> successPayload =
                    ApiResponseDTO.success("Auth status retrieved.", statusResponse);

            return Response.ok(successPayload).build();

        } catch (ApplicationException e) {
            ErrorResponseDTO handledError = ErrorResponseDTO.of(e.getMessage(), e.getErrorCode());
            return Response.status(e.getStatusCode())
                    .entity(handledError)
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during auth-status check", e);
            ErrorResponseDTO fallbackError =
                    ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR");
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(fallbackError)
                    .build();
        }
    }
}

