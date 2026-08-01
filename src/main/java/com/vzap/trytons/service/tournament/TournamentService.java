package com.vzap.trytons.service.tournament;

import com.vzap.trytons.dto.tournament.StartLeagueResponseDTO;
import com.vzap.trytons.dto.tournament.TournamentFixtureResponseDTO;
import com.vzap.trytons.dto.tournament.TournamentResponseDTO;
import com.vzap.trytons.dto.tournament.TournamentSettingsDTO;

import java.util.List;
import java.util.UUID;

public interface TournamentService {

    /**
     * Starts a league and generates its entire pool stage in one go: the draw,
     * the pool tables, and every pool fixture mapped onto upcoming fantasy
     * rounds. Only the league's manager or an administrator may do this, and
     * only while the league is still forming.
     */
    StartLeagueResponseDTO startLeague(UUID actorUserId, UUID leagueId);

    /** The running or most recent tournament of a league, pools and standings included. */
    TournamentResponseDTO getTournamentForLeague(UUID leagueId);

    TournamentResponseDTO getTournament(UUID tournamentId);

    /** Every generated fixture, pool and knockout alike, ordered by matchday. */
    List<TournamentFixtureResponseDTO> getTournamentFixtures(UUID tournamentId);

    /**
     * Recomputes pool standings and, when a stage has finished, generates the
     * next one: pools into the knockout bracket, each knockout round into the
     * next, and the final into a crowned champion.
     *
     * @return true when this call changed the tournament
     */
    boolean advanceTournament(UUID tournamentId);

    /**
     * Advances every tournament that is still running. Called after a round is
     * processed, so progression follows the fantasy calendar automatically.
     *
     * @return how many tournaments were advanced
     */
    int advanceActiveTournaments();

    TournamentSettingsDTO getSettings();

    TournamentSettingsDTO updateSettings(UUID actorUserId, TournamentSettingsDTO request);
}
