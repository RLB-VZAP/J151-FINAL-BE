package com.vzap.trytons.dao;

import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.model.Fixture;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    public Fixture findFixtureById(UUID id) {
        return null;
    }

    @Override
    public Fixture findFixtureByLeagueId(UUID leagueId) {
        return null;
    }

    @Override
    public Fixture findFixtureByDate(LocalDate date) {
        return null;
    }

    @Override
    public Fixture findFixtureByTime(LocalTime time) {
        return null;
    }

    @Override
    public Fixture findFixtureByVenue(String venue) {
        return null;
    }

    @Override
    public Fixture findFixtureByStatus(FixtureStatus status) {
        return null;
    }

    @Override
    public Fixture findFixtureByMatchRoundNumber(int matchRoundNumber) {
        return null;
    }

    @Override
    public Fixture findFixtureByLockDeadline(LocalDateTime lockDeadline) {
        return null;
    }

    @Override
    public Fixture findFixtureByLockStatus(Boolean locked) {
        return null;
    }

    @Override
    public List<Fixture> getAllFixtures() {
        return List.of();
    }

    @Override
    public Optional<Fixture> findFixtureByClub(UUID clubId) {
        return Optional.empty();
    }

    @Override
    public Fixture updateFixture(Fixture fixture) {
        return null;
    }

    @Override
    public void deleteFixture(Fixture fixture) {

    }
}
