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
    //STUB - check all the methods are correct, check return types and add or take away any methods needed or unnecessary
    //ADD ALL METHODS NEEDED
    public Fixture createFixture(Fixture fixture);
    public Fixture findFixtureById(UUID id);
    public Fixture findFixtureByLeagueId(UUID leagueId);
    public Fixture findFixtureByDate(LocalDate date);
    public Fixture findFixtureByTime(LocalTime time);
    public Fixture findFixtureByVenue(String venue);
    public Fixture findFixtureByStatus(FixtureStatus status);
    public Fixture findFixtureByMatchRoundNumber(int matchRoundNumber);
    public Fixture findFixtureByLockDeadline(LocalDateTime lockDeadline);
    public Fixture findFixtureByLockStatus(Boolean locked);
    public List<Fixture> getAllFixtures();
    public Optional<Fixture> findFixtureByClub(UUID clubId);
    public Fixture updateFixture(Fixture fixture);
    public void deleteFixture(Fixture fixture);

}
