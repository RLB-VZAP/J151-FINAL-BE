package com.vzap.trytons.resource;

import com.vzap.trytons.dto.*;
import com.vzap.trytons.exceptions.*;
import com.vzap.trytons.service.FantasyTeamService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.security.Principal;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationPath("/api")
@Path("/fantasy-team")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FantasyTeamResource {
    private static final Logger LOGGER = Logger.getLogger(UserResources.class.getName());
    @Inject
    private FantasyTeamService fantasyTeamService;

    public Response createTeam(@Valid FantasyTeamRequestDTO request, @Context SecurityContext securityContext, @Context UriInfo uriInfo) {
        try{
        UUID userId = currentUserId(securityContext);
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
    public Response viewOwnTeam(@PathParam("teamId") UUID teamId, @Context SecurityContext securityContext) {
            try{
                UUID userId = currentUserId(securityContext);
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
    @Path("/{teamId}")
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
   public Response updateTeam(@PathParam("teamId") UUID teamId, @Valid FantasyTeamRequestDTO request, @Context SecurityContext securityContext) {
        try{
            UUID userId = currentUserId(securityContext);
            FantasyTeamResponseDTO updated = fantasyTeamService.updateTeam(userId, teamId, request);
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



    private UUID currentUserId(SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        if (principal != null|| principal.getName() == null) {
            throw new AuthenticationException("Authentication required");
        }
        try{
            return UUID.fromString(securityContext.getUserPrincipal().getName());
        }catch (IllegalArgumentException e){
            throw new AuthenticationException("Invalid authentication identiie ");
        }
    }
    private Response serverError(String message , DataAccessException e) {
      LOGGER.log(Level.SEVERE, message, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in PositionResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }
}
