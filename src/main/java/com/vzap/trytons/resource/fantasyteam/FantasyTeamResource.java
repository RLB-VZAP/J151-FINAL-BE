package com.vzap.trytons.resource.fantasyteam;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.shared.ErrorResponseDTO;
import com.vzap.trytons.dto.fantasyteam.FantasyTeamRequestDTO;
import com.vzap.trytons.dto.fantasyteam.FantasyTeamResponseDTO;
import com.vzap.trytons.dto.fantasyteam.ViewOpponentTeamDTO;
import com.vzap.trytons.dto.fantasyteam.ViewOwnTeamDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.fantasyteam.FantasyTeamService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Authenticated
@Path("/fantasy-team")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class FantasyTeamResource {

    private static final Logger LOGGER =
            Logger.getLogger(FantasyTeamResource.class.getName());

    @Inject
    private FantasyTeamService fantasyTeamService;

    @POST
    public Response createTeam(
            @Valid FantasyTeamRequestDTO request,
            @Context ContainerRequestContext requestContext,
            @Context UriInfo uriInfo) {

        try {
            UUID userId = currentUserId(requestContext);

            FantasyTeamResponseDTO created =
                    fantasyTeamService.createTeam(userId, request);

            URI location = uriInfo
                    .getAbsolutePathBuilder()
                    .path(created.getTeamId().toString())
                    .build();

            return Response
                    .created(location)
                    .entity(created)
                    .build();

        } catch (AuthenticationException e) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(error(e.getMessage(), "AUTHENTICATION_ERROR"))
                    .build();

        } catch (AuthorisationException e) {
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(error(e.getMessage(), "AUTHORISATION_ERROR"))
                    .build();

        } catch (ConflictException e) {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(error(e.getMessage(), "CONFLICT"))
                    .build();

        } catch (ValidationException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(error(e.getMessage(), "VALIDATION_ERROR"))
                    .build();

        } catch (DataAccessException e) {
            return serverError(
                    "Failed to create fantasy team.",
                    e
            );

        } catch (Exception e) {
            return unexpected(e);
        }
    }

    /*
     * This route is kept separate from the opponent route to prevent
     * ambiguous JAX-RS GET mappings during GlassFish deployment.
     */
    @GET
    @Path("/own/{teamId}")
    public Response viewOwnTeam(
            @PathParam("teamId") UUID teamId,
            @Context ContainerRequestContext requestContext) {

        try {
            UUID userId = currentUserId(requestContext);

            ViewOwnTeamDTO team =
                    fantasyTeamService.viewOwnTeam(userId, teamId);

            return Response.ok(team).build();

        } catch (AuthenticationException e) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(error(e.getMessage(), "AUTHENTICATION_ERROR"))
                    .build();

        } catch (AuthorisationException e) {
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(error(e.getMessage(), "AUTHORISATION_ERROR"))
                    .build();

        } catch (ResourceNotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(error(e.getMessage(), "RESOURCE_NOT_FOUND"))
                    .build();

        } catch (DataAccessException e) {
            return serverError(
                    "Failed to view fantasy team.",
                    e
            );

        } catch (Exception e) {
            return unexpected(e);
        }
    }

    /*
     * This route must remain different from /own/{teamId}.
     */
    @GET
    @Path("/opponent/{teamId}")
    public Response viewOpponentTeam(
            @PathParam("teamId") UUID teamId) {

        try {
            ViewOpponentTeamDTO team =
                    fantasyTeamService.viewOpponentTeam(teamId);

            return Response.ok(team).build();

        } catch (ResourceNotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(error(e.getMessage(), "RESOURCE_NOT_FOUND"))
                    .build();

        } catch (DataAccessException e) {
            return serverError(
                    "Failed to view opponent fantasy team.",
                    e
            );

        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @PUT
    @Path("/{teamId}")
    public Response updateTeam(
            @PathParam("teamId") UUID teamId,
            @Valid FantasyTeamRequestDTO request,
            @Context ContainerRequestContext requestContext) {

        try {
            UUID userId = currentUserId(requestContext);

            FantasyTeamResponseDTO updated =
                    fantasyTeamService.updateTeam(
                            userId,
                            teamId,
                            request
                    );

            return Response.ok(updated).build();

        } catch (AuthenticationException e) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(error(e.getMessage(), "AUTHENTICATION_ERROR"))
                    .build();

        } catch (AuthorisationException e) {
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(error(e.getMessage(), "AUTHORISATION_ERROR"))
                    .build();

        } catch (ResourceNotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(error(e.getMessage(), "RESOURCE_NOT_FOUND"))
                    .build();

        } catch (ConflictException e) {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(error(e.getMessage(), "CONFLICT"))
                    .build();

        } catch (ValidationException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(error(e.getMessage(), "VALIDATION_ERROR"))
                    .build();

        } catch (DataAccessException e) {
            return serverError(
                    "Failed to update fantasy team.",
                    e
            );

        } catch (Exception e) {
            return unexpected(e);
        }
    }

    private UUID currentUserId(
            ContainerRequestContext requestContext) {

        Object currentUser = requestContext.getProperty(
                AuthFilter.CURRENT_USER_PROPERTY
        );

        if (!(currentUser instanceof AuthPrincipal principal)
                || principal.getUserId() == null) {

            throw new AuthenticationException(
                    "Authentication required."
            );
        }

        return principal.getUserId();
    }

    private ErrorResponseDTO error(
            String message,
            String errorCode) {

        return ErrorResponseDTO.of(message, errorCode);
    }

    private Response serverError(
            String message,
            DataAccessException exception) {

        LOGGER.log(Level.SEVERE, message, exception);

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(
                        ErrorResponseDTO.of(
                                message,
                                exception.getErrorCode()
                        )
                )
                .build();
    }

    private Response unexpected(Exception exception) {

        LOGGER.log(
                Level.SEVERE,
                "Unexpected error in FantasyTeamResource.",
                exception
        );

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(
                        ErrorResponseDTO.of(
                                "An unexpected error occurred.",
                                "INTERNAL_SERVER_ERROR"
                        )
                )
                .build();
    }
}