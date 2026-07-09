package com.vzap.trytons.fixture.dao;

import com.vzap.trytons.fixture.model.MatchResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MatchResultDAOImpl implements MatchResultDAO {

    @Override
    public MatchResult save(MatchResult matchResult) {
        throw new UnsupportedOperationException("MatchResultDAOImpl stub: save is not implemented yet.");
    }

    @Override
    public Optional<MatchResult> findById(UUID resultId) {
        throw new UnsupportedOperationException("MatchResultDAOImpl stub: findById is not implemented yet.");
    }

    @Override
    public Optional<MatchResult> findCurrentByFixtureId(UUID fixtureId) {
        throw new UnsupportedOperationException("MatchResultDAOImpl stub: findCurrentByFixtureId is not implemented yet.");
    }

    @Override
    public List<MatchResult> findAllByFixtureId(UUID fixtureId) {
        throw new UnsupportedOperationException("MatchResultDAOImpl stub: findAllByFixtureId is not implemented yet.");
    }

    @Override
    public int getNextSimulationRunNumber(UUID fixtureId) {
        throw new UnsupportedOperationException("MatchResultDAOImpl stub: getNextSimulationRunNumber is not implemented yet.");
    }

    @Override
    public int markAllFixtureResultsNotCurrent(UUID fixtureId) {
        throw new UnsupportedOperationException("MatchResultDAOImpl stub: markAllFixtureResultsNotCurrent is not implemented yet.");
    }

    @Override
    public boolean markResultCurrent(UUID resultId) {
        throw new UnsupportedOperationException("MatchResultDAOImpl stub: markResultCurrent is not implemented yet.");
    }

    @Override
    public boolean approveResult(UUID resultId, UUID approvedByAdminId) {
        throw new UnsupportedOperationException("MatchResultDAOImpl stub: approveResult is not implemented yet.");
    }
}