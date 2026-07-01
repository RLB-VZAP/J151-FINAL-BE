package com.vzap.trytons.dao;

import com.vzap.trytons.model.Player;

import java.util.List;
import java.util.UUID;

public interface FantasyTeamPlayerDAO {
    public void addPlayerToSquad(Player entry);
    public void replaceSquad(UUID teamId, List<Player> players);
    public List<Player> getSquadByTeamId(UUID teamId);
    public Player findSquadEntry(UUID teamId, UUID playerId);
    public void removePlayerFromSquad(UUID teamId, UUID playerId);
}
