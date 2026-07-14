package com.vzap.trytons.dao;

import com.vzap.trytons.model.TeamPlayerSelection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FantasyTeamPlayerSelectionDAO {
    boolean addPlayerToSquad(UUID teamId, UUID playerId);
    void replaceSquad(UUID teamId, List<UUID> playerIds);
    List<TeamPlayerSelection> getSquadByTeamId(UUID teamId);
    Optional<TeamPlayerSelection> findSquadEntry(UUID teamId, UUID playerId);
    boolean removePlayerFromSquad(UUID teamId, UUID playerId);
}
