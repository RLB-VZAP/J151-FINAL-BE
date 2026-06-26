package com.vzap.trytons.resource;

import com.vzap.trytons.dto.RegisteredUserRequest;
import com.vzap.trytons.dto.RegisteredUserResponse;
import com.vzap.trytons.enums.RegistrationStatus;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.RegisteredUser;
import com.vzap.trytons.service.RegisteredUserServices;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;

@ApplicationPath("/api")
@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResources {
    @Inject
    private RegisteredUserServices registeredUserServices;

    @POST
    public Response registerUser(@Valid RegisteredUserRequest request, @Context UriInfo uriInfo) {
        RegisteredUser newUser = RegisteredUser.builder().email(request.getEmail()).username(request.getUsername()).passwordHash(request.getRawPassword()).role(UserRole.REGISTERED_USER).isActive(false).registrationStatus(RegistrationStatus.PENDING).build();
        try {
            RegisteredUser created = registeredUserServices.registeredUser(newUser);
            RegisteredUserResponse body = toResponse(created);
            URI location = uriInfo.getAbsolutePathBuilder().path(created.getUsername()).build();
            return Response.created(location).entity(body).build();
        } catch (ConflictException e) {
            return Response.status(Response.Status.CONFLICT).entity(new ErrorBody(e.getMessage())).build();
        } catch (ValidationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorBody(e.getMessage())).build();
        }

    }

    public static RegisteredUserResponse toResponse(RegisteredUser created) {
        return new RegisteredUserResponse(
                created.getUserId(),
                created.getUsername(),
                created.getRegistrationStatus()
        );
    }

    public static class ErrorBody {
        private String message;

        public ErrorBody(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

}
