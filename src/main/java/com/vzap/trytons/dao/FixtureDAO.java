package com.vzap.trytons.dao;

import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.model.Fixture;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FixtureDAO {
    Fixture create(Fixture fixture);
    Optional<Fixture> findById(UUID fixtureId);
    List<Fixture> findByLeagueId(UUID leagueId);
    List<Fixture> findByRoundId(UUID roundId);
    List<Fixture> findByTeamId(UUID teamId);
    List<Fixture> findByStatus(FixtureStatus status);
    List<Fixture> getAllFixtures();
    Fixture updateFixture(Fixture fixture);
    boolean cancelFixture(UUID fixtureId);
    Fixture updateStatus(Fixture fixture, FixtureStatus status);
}
