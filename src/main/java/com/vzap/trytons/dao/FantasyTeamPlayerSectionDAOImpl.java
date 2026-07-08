package com.vzap.trytons.dao;

import com.vzap.trytons.model.TeamPlayerSelection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FantasyTeamPlayerSectionDAOImpl implements FantasyTeamPlayerSectionDAO {
    @Override
    public boolean addPlayerToSquad(UUID teamId, UUID playerId) {
        return false;
    }

    @Override
    public void replaceSquad(UUID teamId, List<UUID> playerIds) {

    }

    @Override
    public List<TeamPlayerSelection> getSquadByTeamId(UUID teamId) {
        return List.of();
    }

    @Override
    public Optional<TeamPlayerSelection> findSquadEntry(UUID teamId, UUID playerId) {
        return Optional.empty();
    }

    @Override
    public boolean removePlayerFromSquad(UUID teamId, UUID playerId) {
        return false;
    }
}
