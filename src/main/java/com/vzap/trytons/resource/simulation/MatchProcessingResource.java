package com.vzap.trytons.resource.simulation;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.simulation.MatchProcessingResultDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.simulation.MatchProcessingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;
import java.util.UUID;

/**
 * Exposes the existing per-fixture processing step that previously had no REST
 * entry point at all — {@link MatchProcessingService#processCompletedFixture}
 * could only be reached indirectly through
 * {@code POST /competition-processing/due-work}, which processes everything that
 * happens to be due.
 *
 * <p>Without it, an administrator who wanted a single COMPLETED fixture to become
 * PROCESSED had only {@code PUT /fixtures/{id}/status}, which flipped the column
 * and did none of the work the status claims. This resource adds no business
 * logic of its own; it is a thin, admin-only pass-through to the service that
 * calculates fantasy points, writes both {@code match_team_score} rows, refreshes
 * the leaderboard and only then sets PROCESSED.
 */
@ApplicationScoped
@Path("/match-processing")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@AdminOnly
public class MatchProcessingResource {
    @Inject
    private MatchProcessingService matchProcessingService;

    @Context
    private SecurityContext securityContext;

    @POST
    @Path("/fixtures/{fixtureId}")
    public Response processFixture(@PathParam("fixtureId") UUID fixtureId) {
        UUID actorUserId = getAuthenticatedActorUserId();

        MatchProcessingResultDTO result = matchProcessingService.processCompletedFixture(actorUserId, fixtureId);

        return Response.ok(result).build();
    }

    private UUID getAuthenticatedActorUserId() {
        if (securityContext == null) {
            throw new AuthenticationException("An authenticated administrator is required.");
        }

        Principal principal = securityContext.getUserPrincipal();

        if (!(principal instanceof AuthPrincipal authPrincipal)) {
            throw new AuthenticationException("The authenticated user could not be resolved.");
        }

        return authPrincipal.getUserId();
    }
}
