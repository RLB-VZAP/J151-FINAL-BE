package com.vzap.trytons.auth.service;

import com.vzap.trytons.player.dto.PlayerPointsHistoryResponseDTO;
import com.vzap.trytons.auth.dto.UserPointsHistoryResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserHistoryService {

    UserPointsHistoryResponseDTO getUserPointsHistory(UUID actorUserId);
    List<PlayerPointsHistoryResponseDTO> getPlayerPointsHistory(UUID actorUserId, UUID playerId);
}