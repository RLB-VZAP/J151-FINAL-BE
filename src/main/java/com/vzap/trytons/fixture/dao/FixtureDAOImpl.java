package com.vzap.trytons.fixture.dao;
import com.vzap.trytons.shared.dao.BaseDAO;

import com.vzap.trytons.fixture.enums.FixtureStatus;
import com.vzap.trytons.fixture.model.Fixture;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FixtureDAOImpl extends BaseDAO implements FixtureDAO {
    //STUB - Check methods
    @Override
    public Fixture createFixture(Fixture fixture) {
        return null;
    }

    @Override
    public Optional<Fixture> findFixtureById(UUID fixtureId) {
        return Optional.empty();
    }

    @Override
    public List<Fixture> findFixturesByLeagueId(UUID leagueId) {
        return List.of();
    }

    @Override
    public List<Fixture> findFixturesByRoundId(UUID roundId) {
        return List.of();
    }

    @Override
    public List<Fixture> findFixturesByTeamId(UUID teamId) {
        return List.of();
    }

    @Override
    public List<Fixture> findFixturesByStatus(FixtureStatus status) {
        return List.of();
    }

    @Override
    public List<Fixture> getAllFixtures() {
        return List.of();
    }

    @Override
    public Fixture updateFixture(Fixture fixture) {
        return null;
    }

    @Override
    public boolean cancelFixture(UUID fixtureId) {
        return false;
    }
}
