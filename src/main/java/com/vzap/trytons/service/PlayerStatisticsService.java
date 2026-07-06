package com.vzap.trytons.service;

import com.vzap.trytons.dto.PlayerStatisticsRequestDTO;
import com.vzap.trytons.dto.PlayerStatisticsResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PlayerStatisticsService {
    List<PlayerStatisticsResponseDTO> listFixtureStatistics(UUID fixtureId);
    PlayerStatisticsResponseDTO captureStatistic(UUID actorUserId, PlayerStatisticsRequestDTO request);
}
