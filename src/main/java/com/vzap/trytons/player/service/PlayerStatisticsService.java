package com.vzap.trytons.player.service;

import com.vzap.trytons.player.dto.PlayerStatisticsRequestDTO;
import com.vzap.trytons.player.dto.PlayerStatisticsResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PlayerStatisticsService {
    List<PlayerStatisticsResponseDTO> listResultStatistics(UUID resultId);
    List<PlayerStatisticsResponseDTO> listResultStatisticsForTeam(UUID resultId, UUID teamId);
    PlayerStatisticsResponseDTO captureStatistic(UUID actorUserId, PlayerStatisticsRequestDTO request);
}