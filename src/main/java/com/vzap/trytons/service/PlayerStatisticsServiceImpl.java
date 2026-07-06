package com.vzap.trytons.service;

import com.vzap.trytons.dto.PlayerStatisticsRequestDTO;
import com.vzap.trytons.dto.PlayerStatisticsResponseDTO;

import java.util.List;
import java.util.UUID;

public class PlayerStatisticsServiceImpl implements PlayerStatisticsService {
    @Override
    public List<PlayerStatisticsResponseDTO> listFixtureStatistics(UUID fixtureId) {
        return List.of();
    }

    @Override
    public PlayerStatisticsResponseDTO captureStatistic(UUID actorUserId, PlayerStatisticsRequestDTO request) {
        return null;
    }
}
