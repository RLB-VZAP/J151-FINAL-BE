package com.vzap.trytons.fixture.dao;

import com.vzap.trytons.fixture.enums.FixtureStatus;
import com.vzap.trytons.fixture.model.Fixture;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FixtureDAO {
    Fixture createFixture(Fixture fixture);
    Optional<Fixture> findFixtureById(UUID fixtureId);
    List<Fixture> findFixturesByLeagueId(UUID leagueId);
    List<Fixture> findFixturesByRoundId(UUID roundId);
    List<Fixture> findFixturesByTeamId(UUID teamId);
    List<Fixture> findFixturesByStatus(FixtureStatus status);
    List<Fixture> getAllFixtures();
    Fixture updateFixture(Fixture fixture);
    boolean cancelFixture(UUID fixtureId);
}
