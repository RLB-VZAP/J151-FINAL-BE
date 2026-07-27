package com.vzap.trytons.resource.auth;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.auth.RegisteredUserRequestDTO;
import com.vzap.trytons.dto.auth.RegisteredUserResponseDTO;
import com.vzap.trytons.dto.auth.UserSearchResponseDTO;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.model.auth.RegisteredUser;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.auth.RegisteredUserServices;
import com.vzap.trytons.service.auth.UserSearchService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResources {
    @Inject
    private RegisteredUserServices registeredUserServices;

    @Inject
    private UserSearchService userSearchService;

    @POST
    public Response registerUser(@Valid RegisteredUserRequestDTO userRequest, @Context UriInfo uriInfo) {
        RegisteredUser created = registeredUserServices.registerUser(userRequest);
        RegisteredUserResponseDTO body = toResponse(created);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getUsername()).build();
        return Response.created(location).entity(body).build();
    }

    // Privacy-scoped directory search: any authenticated user may look someone
    // up by username (never email) to start a message request. See
    // UserSearchServiceImpl for the matching/limit/exclusion rules.
    @GET
    @Path("/search")
    @Authenticated
    public Response searchUsers(@QueryParam("searchTerm") String searchTerm, @Context ContainerRequestContext requestContext) {
        UUID actorUserId = currentUserId(requestContext);
        List<UserSearchResponseDTO> results = userSearchService.searchUsers(actorUserId, searchTerm);

        return Response.ok(results).build();
    }

    public RegisteredUserResponseDTO toResponse(RegisteredUser created) {
        return new RegisteredUserResponseDTO(created.getUserId(), created.getUsername(), UserRole.REGISTERED_USER ,created.getRegistrationStatus()
        );
    }

    private UUID currentUserId(ContainerRequestContext requestContext) {
        Object currentUser = requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);

        if (!(currentUser instanceof AuthPrincipal principal) || principal.getUserId() == null) {
            throw new AuthenticationException("Authentication required");
        }

        return principal.getUserId();
    }
}
