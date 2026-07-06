package com.vzap.trytons.dao;

import com.vzap.trytons.model.PlayerStatistics;

import java.util.List;
import java.util.UUID;

public class PlayerStatisticsDAOImpl implements PlayerStatisticsDAO {
    @Override
    public List<PlayerStatistics> findByFixtureId(UUID fixtureId) {
        return List.of();
    }

    @Override
    public PlayerStatistics save(PlayerStatistics statistic) {
        return null;
    }
}
