package com.vzap.trytons.dao;

import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.model.Fixture;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FixtureDAOImpl extends BaseDAO implements FixtureDAO {
    @Override
    public Fixture create(Fixture fixture) {
        return null;
    }

    @Override
    public Optional<Fixture> findById(UUID fixtureId) {
        return Optional.empty();
    }

    @Override
    public List<Fixture> findByLeagueId(UUID leagueId) {
        return List.of();
    }

    @Override
    public List<Fixture> findByRoundId(UUID roundId) {
        return List.of();
    }

    @Override
    public List<Fixture> findByTeamId(UUID teamId) {
        return List.of();
    }

    @Override
    public List<Fixture> findByStatus(FixtureStatus status) {
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
