package com.vzap.trytons.player.service;

import com.vzap.trytons.player.dto.PlayerStatisticsRequestDTO;
import com.vzap.trytons.player.dto.PlayerStatisticsResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PlayerStatisticsServiceImpl implements PlayerStatisticsService {

    @Override
    public List<PlayerStatisticsResponseDTO> listResultStatistics(UUID resultId) {
        throw new UnsupportedOperationException("PlayerStatisticsServiceImpl stub: listResultStatistics is not implemented yet.");
    }

    @Override
    public List<PlayerStatisticsResponseDTO> listResultStatisticsForTeam(UUID resultId, UUID teamId) {
        throw new UnsupportedOperationException("PlayerStatisticsServiceImpl stub: listResultStatisticsForTeam is not implemented yet.");
    }

    @Override
    public PlayerStatisticsResponseDTO captureStatistic(UUID actorUserId, PlayerStatisticsRequestDTO request) {
        throw new UnsupportedOperationException("PlayerStatisticsServiceImpl stub: captureStatistic is not implemented yet.");
    }
}