package com.vzap.trytons.dao;

import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.model.Fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
