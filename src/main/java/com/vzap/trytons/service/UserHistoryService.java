package com.vzap.trytons.service;

import com.vzap.trytons.dto.PlayerPointsHistoryResponseDTO;
import com.vzap.trytons.dto.UserPointsHistoryResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserHistoryService {

    UserPointsHistoryResponseDTO getUserPointsHistory(UUID actorUserId);
    List<PlayerPointsHistoryResponseDTO> getPlayerPointsHistory(UUID actorUserId, UUID playerId);
}