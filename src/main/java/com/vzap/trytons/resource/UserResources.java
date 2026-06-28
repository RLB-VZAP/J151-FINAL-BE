package com.vzap.trytons.resource;

import com.vzap.trytons.dto.ErrorResponse;
import com.vzap.trytons.dto.RegisteredUserRequest;
import com.vzap.trytons.dto.RegisteredUserResponse;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.RegisteredUser;
import com.vzap.trytons.service.RegisteredUserServices;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO move @ApplicationPath("/api") to RestApplication in BE07; resource classes should only define endpoint paths
@ApplicationPath("/api")
@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResources {

    private static final Logger LOGGER = Logger.getLogger(UserResources.class.getName());

    @Inject
    private RegisteredUserServices registeredUserServices;
    @POST
    public Response registerUser(@Valid RegisteredUserRequest request, @Context UriInfo uriInfo) {
        try {
            RegisteredUser created = registeredUserServices.registerUser(request);
            RegisteredUserResponse body = toResponse(created);
            URI location = uriInfo.getAbsolutePathBuilder().path(created.getUsername()).build();
            return Response.created(location)
                    .entity(body)
                    .build();
        } catch (ConflictException e) {
            ErrorResponse error = ErrorResponse.of(e.getMessage(), e.getErrorCode());
            return Response.status(Response.Status.CONFLICT)
                    .entity(error)
                    .build();
        } catch (ValidationException e) {
            ErrorResponse error = ErrorResponse.of(e.getMessage(), e.getErrorCode());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
        } catch (DataAccessException e) {
            LOGGER.log(Level.SEVERE, "Registration failed to save.", e);
            ErrorResponse error = ErrorResponse.of("Registration failed.", e.getErrorCode());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during user registration.", e);
            ErrorResponse error = ErrorResponse.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }

    public RegisteredUserResponse toResponse(RegisteredUser created) {
        return new RegisteredUserResponse(created.getUserId(), created.getUsername(), created.getRegistrationStatus()
        );
    }
}