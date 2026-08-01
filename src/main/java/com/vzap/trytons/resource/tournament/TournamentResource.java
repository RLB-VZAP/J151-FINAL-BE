package com.vzap.trytons.resource.tournament;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.tournament.StartLeagueResponseDTO;
import com.vzap.trytons.dto.tournament.TournamentFixtureResponseDTO;
import com.vzap.trytons.dto.tournament.TournamentResponseDTO;
import com.vzap.trytons.dto.tournament.TournamentSettingsDTO;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.tournament.TournamentService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/tournaments")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TournamentResource {

    @Inject
    private TournamentService tournamentService;

    @Context
    private ContainerRequestContext request;

    private UUID getCurrentUserId() {
        AuthPrincipal principal = (AuthPrincipal) request.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        return principal.getUserId();
    }

    /**
     * Starts a league, generating its pools and every pool fixture. Open to the
     * league's own manager as well as to administrators.
     */
    @POST
    @Path("/leagues/{leagueId}/start")
    public Response startLeague(@PathParam("leagueId") UUID leagueId) {
        StartLeagueResponseDTO started = tournamentService.startLeague(getCurrentUserId(), leagueId);
        return Response.status(Response.Status.CREATED).entity(started).build();
    }

    @GET
    @Path("/leagues/{leagueId}")
    public Response getTournamentForLeague(@PathParam("leagueId") UUID leagueId) {
        TournamentResponseDTO tournament = tournamentService.getTournamentForLeague(leagueId);
        return Response.ok(tournament).build();
    }

    @GET
    @Path("/{tournamentId}")
    public Response getTournament(@PathParam("tournamentId") UUID tournamentId) {
        TournamentResponseDTO tournament = tournamentService.getTournament(tournamentId);
        return Response.ok(tournament).build();
    }

    @GET
    @Path("/{tournamentId}/fixtures")
    public Response getTournamentFixtures(@PathParam("tournamentId") UUID tournamentId) {
        List<TournamentFixtureResponseDTO> fixtures = tournamentService.getTournamentFixtures(tournamentId);
        return Response.ok(fixtures).build();
    }

    /**
     * Forces a progression pass. Tournaments advance themselves as rounds are
     * processed, so this exists for administrators to re-run the check.
     */
    @POST
    @Path("/{tournamentId}/advance")
    @AdminOnly
    public Response advanceTournament(@PathParam("tournamentId") UUID tournamentId) {
        boolean advanced = tournamentService.advanceTournament(tournamentId);
        return Response.ok(tournamentService.getTournament(tournamentId))
                .header("X-Tournament-Advanced", advanced)
                .build();
    }

    @GET
    @Path("/settings")
    public Response getSettings() {
        TournamentSettingsDTO settings = tournamentService.getSettings();
        return Response.ok(settings).build();
    }

    @PUT
    @Path("/settings")
    @AdminOnly
    public Response updateSettings(@Valid TournamentSettingsDTO request) {
        TournamentSettingsDTO updated = tournamentService.updateSettings(getCurrentUserId(), request);
        return Response.ok(updated).build();
    }
}
