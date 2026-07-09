package com.vzap.trytons.player.dao;

import com.vzap.trytons.player.model.PlayerStatistics;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerStatisticsDAO {
    PlayerStatistics save(PlayerStatistics playerStatistics);
    Optional<PlayerStatistics> findById(UUID statId);
    List<PlayerStatistics> findByResultId(UUID resultId);
    List<PlayerStatistics> findByResultIdAndTeamId(UUID resultId, UUID teamId);
    Optional<PlayerStatistics> findByResultIdAndTeamIdAndPlayerId(UUID resultId, UUID teamId, UUID playerId);
}