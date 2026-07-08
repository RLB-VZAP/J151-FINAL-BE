package com.vzap.trytons.dao;

import com.vzap.trytons.model.MatchResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MatchResultDAOImpl implements MatchResultDAO {

    @Override
    public MatchResult save(MatchResult matchResult) {
        return null;
    }

    @Override
    public Optional<MatchResult> findById(UUID resultId) {
        return Optional.empty();
    }

    @Override
    public Optional<MatchResult> findCurrentByFixtureId(UUID fixtureId) {
        return Optional.empty();
    }

    @Override
    public List<MatchResult> findAllByFixtureId(UUID fixtureId) {
        return List.of();
    }

    @Override
    public int getNextSimulationRunNumber(UUID fixtureId) {
        return 0;
    }

    @Override
    public int markAllFixtureResultsNotCurrent(UUID fixtureId) {
        return 0;
    }

    @Override
    public boolean markResultCurrent(UUID resultId) {
        return false;
    }

    @Override
    public boolean approveResult(UUID resultId, UUID approvedByAdminId) {
        return false;
    }
}
