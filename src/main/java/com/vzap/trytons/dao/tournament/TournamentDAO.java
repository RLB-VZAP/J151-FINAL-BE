package com.vzap.trytons.dao.tournament;

import com.vzap.trytons.enums.TournamentStatus;
import com.vzap.trytons.model.tournament.Tournament;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentDAO {
    Tournament create(Tournament tournament);

    Optional<Tournament> findById(UUID tournamentId);

    Optional<Tournament> findByLeagueAndSeason(UUID leagueId, String season);

    /** The POOL_STAGE or KNOCKOUT_STAGE tournament of a league, if one is running. */
    Optional<Tournament> findActiveByLeague(UUID leagueId);

    List<Tournament> findByLeague(UUID leagueId);

    List<Tournament> findByStatus(TournamentStatus status);

    boolean updateStatus(UUID tournamentId, TournamentStatus status);

    /**
     * Removes a tournament and, by cascade, its pools, members, standings and
     * generated fixtures. Used to undo a half-written generation run.
     */
    boolean delete(UUID tournamentId);

    /** Crowns the tournament. Third place may be null when no playoff was played. */
    boolean complete(UUID tournamentId, UUID championTeamId, UUID runnerUpTeamId, UUID thirdPlaceTeamId);
}
