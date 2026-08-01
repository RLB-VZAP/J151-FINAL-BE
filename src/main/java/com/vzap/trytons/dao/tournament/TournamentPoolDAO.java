package com.vzap.trytons.dao.tournament;

import com.vzap.trytons.model.tournament.TournamentPool;
import com.vzap.trytons.model.tournament.TournamentPoolMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentPoolDAO {
    TournamentPool create(TournamentPool pool);

    Optional<TournamentPool> findById(UUID poolId);

    List<TournamentPool> findByTournament(UUID tournamentId);

    TournamentPoolMember addMember(TournamentPoolMember member);

    List<TournamentPoolMember> findMembersByPool(UUID poolId);

    List<TournamentPoolMember> findMembersByTournament(UUID tournamentId);
}
