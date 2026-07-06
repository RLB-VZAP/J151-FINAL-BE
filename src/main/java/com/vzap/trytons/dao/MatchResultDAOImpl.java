package com.vzap.trytons.dao;

import com.vzap.trytons.model.MatchResult;

import java.util.Optional;
import java.util.UUID;

public class MatchResultDAOImpl implements MatchResultDAO {
    @Override
    public Optional<MatchResult> findByFixtureId(UUID fixtureId) {
        return Optional.empty();
    }

    @Override
    public MatchResult save(MatchResult matchResult) {
        return null;
    }
}
