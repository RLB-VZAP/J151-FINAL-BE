package com.vzap.trytons.resource.simulation;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.simulation.CompetitionProcessingSummaryDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.simulation.CompetitionProcessingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;
import java.util.UUID;

@ApplicationScoped
@Path("/competition-processing")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@AdminOnly
public class CompetitionProcessingResource {

    @Inject
    private CompetitionProcessingService competitionProcessingService;

    @Context
    private SecurityContext securityContext;

    /**
     * Finds and processes all competition work that is currently due.
     *
     * This may include:
     * - locking eligible rounds;
     * - simulating unprocessed locked fixtures;
     * - persisting results and player statistics;
     * - calculating fantasy points and team scores;
     * - refreshing leaderboards.
     *
     * The underlying service must ensure that repeating this request does not
     * duplicate previously completed processing.
     */
    @POST
    @Path("/due-work")
    public Response processDueWork() {
        UUID actorUserId = getAuthenticatedActorUserId();

        CompetitionProcessingSummaryDTO summary = competitionProcessingService.processDueWork(actorUserId);

        return Response.ok(summary).build();
    }

    /**
     * Retrieves the authenticated administrator's user ID from the request.
     */
    private UUID getAuthenticatedActorUserId() {
        if (securityContext == null) {
            throw new AuthenticationException("An authenticated administrator is required.");
        }

        Principal principal = securityContext.getUserPrincipal();

        if (!(principal instanceof AuthPrincipal)) {
            throw new AuthenticationException("The authenticated user could not be resolved.");
        }

        AuthPrincipal authPrincipal = (AuthPrincipal) principal;

        return authPrincipal.getUserId();
    }
}