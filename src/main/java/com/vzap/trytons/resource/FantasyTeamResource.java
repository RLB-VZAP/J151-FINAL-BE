package com.vzap.trytons.resource;

import com.vzap.trytons.dto.ErrorResponse;
import com.vzap.trytons.dto.FantasyTeamPlayerSelectionRequestDTO;
import com.vzap.trytons.dto.FantasyTeamRequestDTO;
import com.vzap.trytons.dto.FantasyTeamResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ValidationException;
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
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponse.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }
}
