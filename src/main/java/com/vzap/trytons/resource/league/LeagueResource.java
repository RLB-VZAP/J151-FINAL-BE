package com.vzap.trytons.resource.league;

import com.vzap.trytons.Annotations.Authenticated;
import com.vzap.trytons.dto.shared.ErrorResponseDTO;
import com.vzap.trytons.dto.league.JoinLeagueRequestDTO;
import com.vzap.trytons.dto.league.JoinLeagueResponseDTO;
import com.vzap.trytons.dto.league.LeagueMemberResponseDTO;
import com.vzap.trytons.dto.league.LeagueRequestDTO;
import com.vzap.trytons.dto.league.LeagueResponseDTO;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.league.LeagueService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


@Path("/league")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeagueResource {
    private static final Logger LOG = Logger.getLogger(LeagueResource.class.getName());

    @Inject
    private LeagueService leagueService;

    @Context
    private ContainerRequestContext request;
    private UUID getCurrentUserId(){
        AuthPrincipal principal = (AuthPrincipal) request.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        return principal.getUserId();
    }

    @POST
    public Response createLeague(@Valid LeagueRequestDTO request,@Context UriInfo uriInfo) {
        try{
            LeagueResponseDTO created = leagueService.createLeague(request,getCurrentUserId());
            URI location = uriInfo.getAbsolutePathBuilder().path(created.getLeagueId().toString()).build();
            return Response.created(location).entity(created).build();
        }catch(ConflictException e){
            return Response.status(Response.Status.CONFLICT).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        }catch(DataAccessException e){
            return serverError("Failed to create League", e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @GET
    @Path("/{id}")
    public Response getLeague(@PathParam("id") UUID id) {
        try {
            LeagueResponseDTO league = leagueService.getLeague(id, getCurrentUserId());
            return Response.ok(league).build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        }catch(DataAccessException e){
            return serverError("Failed to get League", e);
        }catch(Exception e){
            return unexpected(e);
        }
    }

    @GET
    public Response getAllLeagues(@QueryParam("mine") boolean mine) {
        try{
            List<LeagueResponseDTO> leagues = mine
                    ? leagueService.getMyLeagues(getCurrentUserId())
                    : leagueService.getAllLeagues(getCurrentUserId());
            return Response.ok(leagues).build();
        }catch(DataAccessException e){
            return serverError("Failed to get Leagues", e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @POST
    @Path("/join")
    public Response joinLeague(@Valid JoinLeagueRequestDTO request) {
        try {
            JoinLeagueResponseDTO response = leagueService.joinLeague(request, getCurrentUserId());
            return Response.status(Response.Status.OK)
                    .entity(response)
                    .build();
        }catch(ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        }catch(ConflictException e){
            return Response.status(Response.Status.CONFLICT).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        }catch(DataAccessException e){
            return serverError("Failed to join League", e);
        }catch(Exception e){
            return unexpected(e);
        }
    }

    private Response serverError(String message, DataAccessException e) {
        LOG.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of(message, e.getErrorCode())).build();
    }

    private Response unexpected(Exception e) {
        LOG.log(Level.SEVERE, "Unexpected error in LeagueResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }

    @GET
    @Path("/{id}/members")
    public Response listMembers(@PathParam("id") String id) {
        try {
            List<LeagueMemberResponseDTO> members = leagueService.listMembers(getCurrentUserId().toString(), id);
            return Response.ok(members).build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        } catch (AuthorisationException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        } catch (DataAccessException e) {
            return serverError("Failed to list members", e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @DELETE
    @Path("/{id}/members/{membershipId}")
    public Response removeMember(@PathParam("id") String id, @PathParam("membershipId") String membershipId) {
        try {
            leagueService.removeMember(getCurrentUserId().toString(), id, membershipId);
            return Response.noContent().build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        } catch (AuthorisationException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        } catch (DataAccessException e) {
            return serverError("Failed to remove member", e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @GET
    @Path("/{id}/code")
    public Response getLeagueCode(@PathParam("id") String id) {
        try {
            String code = leagueService.getLeagueCode(getCurrentUserId().toString(), id);
            return Response.ok(java.util.Map.of("leagueCode", code)).build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        } catch (AuthorisationException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(ErrorResponseDTO.of(e.getMessage(), e.getErrorCode())).build();
        } catch (DataAccessException e) {
            return serverError("Failed to get league code", e);
        } catch (Exception e) {
            return unexpected(e);
        }
    }

}
