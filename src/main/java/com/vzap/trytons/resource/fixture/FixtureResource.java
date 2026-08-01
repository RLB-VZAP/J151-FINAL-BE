package com.vzap.trytons.resource.fixture;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.fixture.FixtureResponseDTO;
import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.fixture.FixtureService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/fixtures")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FixtureResource {
    @Inject
    private FixtureService fixtureService;
    @Context
    private ContainerRequestContext request;

    private UUID getCurrentUserId(){
        AuthPrincipal principal = (AuthPrincipal) request.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        return principal.getUserId();
    }

    @GET
    public Response listFixtures(@QueryParam("status") FixtureStatus status, @QueryParam("leagueId") UUID leagueId){
        List<FixtureResponseDTO>fixtures = fixtureService.listFixtures(getCurrentUserId(), status, leagueId);
        return Response.ok(fixtures)
                .build();
    }

    @GET
    @Path("/{fixtureId}")
    public Response getFixture(@PathParam("fixtureId") UUID fixtureId){
        FixtureResponseDTO fixture = fixtureService.getFixture(fixtureId);
        return Response.ok(fixture).build();
    }

    /*
        Fixtures are no longer created by hand. Every fixture now comes from
        tournament generation, which draws the pools and schedules the whole
        competition, so an administrator may view and update a fixture but not
        create one. Creating fixtures outside a tournament would also break the
        one-fixture-per-team-per-round rule the generator depends on.
    */

    @PUT
    @Path("/{fixtureId}/status")
    @AdminOnly
    public Response updateFixtureStatus(@PathParam("fixtureId") UUID fixtureId, @QueryParam("status") FixtureStatus status){
        FixtureResponseDTO updated = fixtureService.updateFixtureStatus(getCurrentUserId(),fixtureId,status);
        return Response.ok(updated)
                .build();
    }

}
