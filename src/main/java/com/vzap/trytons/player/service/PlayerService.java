package com.vzap.trytons.player.service;

import com.vzap.trytons.player.dto.PlayerRequestDTO;
import com.vzap.trytons.player.dto.PlayerResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PlayerService {
    PlayerResponseDTO createPlayer(PlayerRequestDTO request);
    PlayerResponseDTO getPlayer(UUID playerId);
    List<PlayerResponseDTO> getAllPlayers();
    List<PlayerResponseDTO> searchPlayers(String playerName, UUID clubId, UUID positionId);
    PlayerResponseDTO updatePlayer(UUID playerId, PlayerRequestDTO request);
}