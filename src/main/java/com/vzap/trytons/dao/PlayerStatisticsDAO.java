package com.vzap.trytons.dao;

import com.vzap.trytons.model.PlayerStatistics;

import java.util.List;
import java.util.UUID;

public interface PlayerStatisticsDAO {
    List<PlayerStatistics> findByFixtureId(UUID fixtureId);
    PlayerStatistics save(PlayerStatistics statistic);
}
