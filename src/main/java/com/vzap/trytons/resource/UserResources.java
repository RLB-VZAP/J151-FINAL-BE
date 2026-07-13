package com.vzap.trytons.resource;

import com.vzap.trytons.dto.ErrorResponseDTO;
import com.vzap.trytons.dto.RegisteredUserRequestDTO;
import com.vzap.trytons.dto.RegisteredUserResponseDTO;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.RegisteredUser;
import com.vzap.trytons.service.RegisteredUserServices;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResources {

    private static final Logger LOGGER = Logger.getLogger(UserResources.class.getName());

    @Inject
    private RegisteredUserServices registeredUserServices;
    @POST
    public Response registerUser(@Valid RegisteredUserRequestDTO userRequest, @Context UriInfo uriInfo) {
        try {
            RegisteredUser created = registeredUserServices.registerUser(userRequest);
            RegisteredUserResponseDTO body = toResponse(created);
            URI location = uriInfo.getAbsolutePathBuilder().path(created.getUsername()).build();
            return Response.created(location).entity(body).build();
        } catch (ConflictException e) {
            ErrorResponseDTO error = ErrorResponseDTO.of(e.getMessage(), e.getErrorCode());
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        } catch (DataAccessException e) {
            LOGGER.log(Level.SEVERE, "Registration failed to save.", e);
            ErrorResponseDTO error = ErrorResponseDTO.of("Registration failed.", e.getErrorCode());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during user registration.", e);
            ErrorResponseDTO error = ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    public RegisteredUserResponseDTO toResponse(RegisteredUser created) {
        return new RegisteredUserResponseDTO(created.getUserId(), created.getUsername(), UserRole.REGISTERED_USER ,created.getRegistrationStatus()
        );
    }
}