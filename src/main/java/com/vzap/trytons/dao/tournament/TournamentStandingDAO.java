package com.vzap.trytons.dao.tournament;

import com.vzap.trytons.model.tournament.TournamentStanding;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentStandingDAO {
    TournamentStanding create(TournamentStanding standing);

    /** Rewrites the mutable counters, points, position and qualification flag. */
    boolean update(TournamentStanding standing);

    Optional<TournamentStanding> findByPoolAndTeam(UUID poolId, UUID teamId);

    List<TournamentStanding> findByPool(UUID poolId);

    List<TournamentStanding> findByTournament(UUID tournamentId);
}
