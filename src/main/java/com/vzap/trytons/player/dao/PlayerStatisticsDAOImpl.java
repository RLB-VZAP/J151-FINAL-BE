package com.vzap.trytons.player.dao;

import com.vzap.trytons.player.model.PlayerStatistics;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerStatisticsDAOImpl implements PlayerStatisticsDAO {

    @Override
    public PlayerStatistics save(PlayerStatistics playerStatistics) {
        throw new UnsupportedOperationException("PlayerStatisticsDAOImpl stub: save is not implemented yet.");
    }

    @Override
    public Optional<PlayerStatistics> findById(UUID statId) {
        throw new UnsupportedOperationException("PlayerStatisticsDAOImpl stub: findById is not implemented yet.");
    }

    @Override
    public List<PlayerStatistics> findByResultId(UUID resultId) {
        throw new UnsupportedOperationException("PlayerStatisticsDAOImpl stub: findByResultId is not implemented yet.");
    }

    @Override
    public List<PlayerStatistics> findByResultIdAndTeamId(UUID resultId, UUID teamId) {
        throw new UnsupportedOperationException("PlayerStatisticsDAOImpl stub: findByResultIdAndTeamId is not implemented yet.");
    }

    @Override
    public Optional<PlayerStatistics> findByResultIdAndTeamIdAndPlayerId(UUID resultId, UUID teamId, UUID playerId) {
        throw new UnsupportedOperationException("PlayerStatisticsDAOImpl stub: findByResultIdAndTeamIdAndPlayerId is not implemented yet.");
    }
}