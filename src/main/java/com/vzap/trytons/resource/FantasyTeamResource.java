package com.vzap.trytons.resource;

import com.vzap.trytons.Annotations.Authenticated;
import com.vzap.trytons.dto.*;
import com.vzap.trytons.exceptions.*;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.FantasyTeamService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
@Authenticated
@Path("/fantasy-team")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class FantasyTeamResource {
    private static final Logger LOGGER = Logger.getLogger(FantasyTeamResource.class.getName());
    @Inject
    private FantasyTeamService fantasyTeamService;
    @POST
    public Response createTeam(@Valid FantasyTeamRequestDTO request,@Context ContainerRequestContext requestContext ,@Context UriInfo uriInfo) {
        try{
        UUID userId = currentUserId(requestContext);
        FantasyTeamResponseDTO created = fantasyTeamService.createTeam(userId, request);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getTeamId().toString()).build();
        return Response.created(location).entity(created).build();
        }catch(AuthenticationException e){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }catch(DataAccessException e){
          return serverError("Failed to create fantasy team",e);
        }catch(ConflictException e){
            return Response.status(Response.Status.CONFLICT).build();
        }catch (ValidationException e){
           return Response.status(Response.Status.BAD_REQUEST).build();
        }
        catch(Exception e){
            return unexpected(e);
        }
    }

    @GET
    @Path("/{teamId}")
    public Response viewOwnTeam(@PathParam("teamId") UUID teamId, @Context ContainerRequestContext requestContext) {
            try{
                UUID userId = currentUserId(requestContext);
                ViewOwnTeamDTO team = fantasyTeamService.viewOwnTeam(userId, teamId);
                return Response.ok(team).build();
            }catch(AuthenticationException e){
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }catch(ResourceNotFoundException e){
                return Response.status(Response.Status.NOT_FOUND).build();
            }catch(DataAccessException e){
                return serverError("Failed to view team",e);
            }catch(Exception e){
                return unexpected(e);
            }
    }

    @GET
    @Path("/{teamId}/opponent")
    public Response viewOpponentTeam(@PathParam("teamId") UUID teamId){
        try{
            ViewOpponentTeamDTO team = fantasyTeamService.viewOpponentTeam(teamId);
            return Response.ok(team).build();
        }catch(ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).build();
        }catch(DataAccessException e){
            return serverError("Failed to view team",e);
        }catch(Exception e){
            return unexpected(e);
        }
    }

   @PUT
   @Path("/{teamId}")
   public Response updateTeam(@PathParam("teamId") UUID teamId, @Valid FantasyTeamRequestDTO request, @Context ContainerRequestContext requestContext) {
        try{

            FantasyTeamResponseDTO updated = fantasyTeamService.updateTeam(currentUserId(requestContext), teamId, request);
            return Response.ok(updated).build();
        }catch (ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch(AuthenticationException e){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }catch(DataAccessException e){
            return serverError("Failed to update team",e);
        }catch(Exception e){
            return unexpected(e);
        }
   }

    private UUID currentUserId(ContainerRequestContext requestContext) {
        Object currentUser = requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        if (!(currentUser instanceof AuthPrincipal principal) || principal.getUserId() == null) {
            throw new AuthenticationException("Authentication required");
        }
        return principal.getUserId();
    }

    private Response serverError(String message, DataAccessException e) {
        LOGGER.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of(message, e.getErrorCode())).build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in FantasyTeamResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }
}
