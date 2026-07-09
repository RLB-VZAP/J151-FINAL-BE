package com.vzap.trytons.roster.dao;

import com.vzap.trytons.roster.model.TeamPlayerSelection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FantasyTeamPlayerSectionDAO {
    boolean addPlayerToSquad(UUID teamId, UUID playerId);
    void replaceSquad(UUID teamId, List<UUID> playerIds);
    List<TeamPlayerSelection> getSquadByTeamId(UUID teamId);
    Optional<TeamPlayerSelection> findSquadEntry(UUID teamId, UUID playerId);
    boolean removePlayerFromSquad(UUID teamId, UUID playerId);
}
