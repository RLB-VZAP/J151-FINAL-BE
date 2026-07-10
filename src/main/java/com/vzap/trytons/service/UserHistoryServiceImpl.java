package com.vzap.trytons.service;

import com.vzap.trytons.dto.PlayerPointsHistoryResponseDTO;
import com.vzap.trytons.dto.UserPointsHistoryResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserHistoryServiceImpl implements UserHistoryService {

    @Override
    public UserPointsHistoryResponseDTO getUserPointsHistory(UUID actorUserId) {
        return new UserPointsHistoryResponseDTO();
    }

    @Override
    public List<PlayerPointsHistoryResponseDTO> getPlayerPointsHistory(UUID actorUserId, UUID playerId) {
        return Collections.emptyList();
    }
}
