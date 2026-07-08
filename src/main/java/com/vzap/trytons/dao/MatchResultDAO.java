package com.vzap.trytons.dao;

import com.vzap.trytons.model.MatchResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchResultDAO {
    MatchResult save(MatchResult matchResult);
    Optional<MatchResult> findById(UUID resultId);
    Optional<MatchResult> findCurrentByFixtureId(UUID fixtureId);
    List<MatchResult> findAllByFixtureId(UUID fixtureId);
    int getNextSimulationRunNumber(UUID fixtureId);
    int markAllFixtureResultsNotCurrent(UUID fixtureId);
    boolean markResultCurrent(UUID resultId);
    boolean approveResult(UUID resultId, UUID approvedByAdminId);
}