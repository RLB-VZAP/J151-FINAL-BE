package com.vzap.trytons.dao;

import com.vzap.trytons.model.MatchResult;

import java.util.Optional;
import java.util.UUID;

public interface MatchResultDAO {

    Optional<MatchResult> findByFixtureId(UUID fixtureId);
    Optional<MatchResult> save(MatchResult matchResult);
}
