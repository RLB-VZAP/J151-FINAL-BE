package com.vzap.trytons.service;

import com.vzap.trytons.dto.PlayerRequestDTO;
import com.vzap.trytons.dto.PlayerResponseDTO;
import com.vzap.trytons.model.Player;

import java.util.List;
import java.util.UUID;

public interface PlayerService {
    PlayerResponseDTO createPlayer(PlayerRequestDTO request);
    List<Player> search(String search, UUID clubId, UUID positionId);
    PlayerResponseDTO getPlayer(UUID playerId);
    List<PlayerResponseDTO> getAllPlayers();
    PlayerResponseDTO updatePlayer(UUID playerId, PlayerRequestDTO request);
}
