package com.vzap.trytons.resource;

import com.vzap.trytons.Annotations.AdminOnly;
import com.vzap.trytons.Annotations.Authenticated;
import com.vzap.trytons.dto.ErrorResponseDTO;
import com.vzap.trytons.dto.FixtureRequestDTO;
import com.vzap.trytons.dto.FixtureResponseDTO;
import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.exceptions.*;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.FixtureService;
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

@Path("/fixtures")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FixtureResource {
    private static final Logger LOG = Logger.getLogger(FixtureResource.class.getName());
    @Inject
    private FixtureService fixtureService;
    @Context
    private ContainerRequestContext request;

    private UUID getCurrentUserId(){
        AuthPrincipal principal = (AuthPrincipal) request.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        return principal.getUserId();
    }

    @GET
    public Response listFixtures(@QueryParam("status")FixtureStatus status){
        try{
            List<FixtureResponseDTO>fixtures = fixtureService.listFixtures(status);
            return Response.ok(fixtures).build();
        }catch(DataAccessException e){
            return serverError("Failed to load fixtures",e);
        }catch(Exception e){
            return unexpected(e);
        }
    }
    @GET
    @Path("/{fixtureId}")
    public Response getFixture(@PathParam("fixtureId") UUID fixtureId){
        try{
            FixtureResponseDTO fixture = fixtureService.getFixture(fixtureId);
            return Response.ok(fixture).build();
        }catch(ValidationException e){
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(DataAccessException e){
            return serverError("Failed to load fixture",e);
        }catch(Exception e){
            return unexpected(e);
        }
    }
    @POST
    @AdminOnly
    public Response createFixture(@Valid FixtureRequestDTO request, @Context UriInfo uriInfo){
        try {
            FixtureResponseDTO created = fixtureService.createFixture(getCurrentUserId(),request);
            URI location = uriInfo.getAbsolutePathBuilder().path(created.getFixtureId().toString()).build();
            return Response.created(location).entity(created).build();
        }catch(AuthorisationException e){
            return Response.status(Response.Status.FORBIDDEN).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(ValidationException e){
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(ConflictException e){
            return Response.status(Response.Status.CONFLICT).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(DataAccessException e){
            return serverError("Failed to create fixture",e);
        }catch(Exception e){
            return unexpected(e);
        }
    }

    @PUT
    @Path("/{fixtureId}/status")
    @AdminOnly
    public Response updateFixtureStatus(@PathParam("fixtureId") UUID fixtureId, @QueryParam("status") FixtureStatus status){
        try{
            FixtureResponseDTO updated = fixtureService.updateFixtureStatus(getCurrentUserId(),fixtureId,status);
            return Response.ok(updated).build();
        }catch(AuthorisationException e){
            return Response.status(Response.Status.FORBIDDEN).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(ValidationException e){
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(BusinessRuleException e){
            return Response.status(422).entity(ErrorResponseDTO.of(e.getMessage(),e.getErrorCode())).build();
        }catch(DataAccessException e){
            return serverError("Failed to create fixture",e);
        }catch(Exception e){
            return unexpected(e);
        }
    }
    private Response serverError(String message, DataAccessException e){
        LOG.log(Level.SEVERE,message,e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.of(message, e.getErrorCode())).build();
    }
    private Response unexpected(Exception e){
        LOG.log(Level.SEVERE,"Unexpected error in FixtureResource",e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponseDTO.internalServerError()).build();
    }

}
