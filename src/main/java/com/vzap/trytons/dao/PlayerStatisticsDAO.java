package com.vzap.trytons.dao;

import com.vzap.trytons.model.PlayerStatistics;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerStatisticsDAO {

    List<PlayerStatistics> findByFixtureId(UUID fixtureId);
    Optional<PlayerStatistics> save(PlayerStatistics playerStatistics);
}
