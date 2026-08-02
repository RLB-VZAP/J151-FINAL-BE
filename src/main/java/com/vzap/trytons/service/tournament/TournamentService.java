package com.vzap.trytons.service.tournament;

import com.vzap.trytons.dto.tournament.StartLeagueResponseDTO;
import com.vzap.trytons.dto.tournament.TournamentFixtureResponseDTO;
import com.vzap.trytons.dto.tournament.TournamentResponseDTO;
import com.vzap.trytons.dto.tournament.TournamentSettingsDTO;

import com.vzap.trytons.dto.tournament.MatchDayResponseDTO;

import java.time.LocalDate;
import java.time.LocalTime;
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

    /**
     * Moves one fantasy round -- and every fixture in it -- to another match
     * day. The editable unit is the round rather than the fixture, so "all the
     * fixtures of a round are played on the same day" holds by construction.
     *
     * <p>Administrators may move any league's rounds; a PRIVATE league's own
     * manager may move theirs. A round may only be moved while it is still
     * UPCOMING, nothing in it has been played, the new day is a legal match day
     * still in the future, and the move keeps the tournament in the order it
     * was drawn.
     */
    MatchDayResponseDTO updateMatchDay(UUID actorUserId, UUID leagueId, UUID roundId,
                                       LocalDate matchDay, LocalTime kickoff);

    /**
     * The administrator "play this round now" override: drags a round's window
     * back to the present so the next {@code POST /competition-processing/due-work}
     * opens, locks and plays it. Presentation and testing tool -- deliberately
     * admin only, and deliberately separate from {@link #updateMatchDay} so the
     * two authorisation rules never blur into one.
     */
    MatchDayResponseDTO playRoundNow(UUID actorUserId, UUID roundId);

    TournamentSettingsDTO getSettings();

    TournamentSettingsDTO updateSettings(UUID actorUserId, TournamentSettingsDTO request);
}
