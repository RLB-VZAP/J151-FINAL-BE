package com.vzap.trytons.service;

import com.vzap.trytons.dto.PlayerRequestDTO;
import com.vzap.trytons.dto.PlayerResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PlayerService {
    PlayerResponseDTO createPlayer(PlayerRequestDTO request);
    PlayerResponseDTO getPlayer(UUID playerId);
    List<PlayerResponseDTO> getAllPlayers();
    PlayerResponseDTO updatePlayer(UUID playerId, PlayerRequestDTO request);
}
