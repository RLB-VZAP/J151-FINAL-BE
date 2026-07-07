package com.vzap.trytons.service;

import com.vzap.trytons.dao.MatchResultDAO;
import com.vzap.trytons.model.MatchResult;

import java.util.Optional;
import java.util.UUID;

public class MatchResultServiceImpl implements MatchResultDAO {
    @Override
    public Optional<MatchResult> findByFixtureId(UUID fixtureId) {
        return Optional.empty();
    }

    @Override
    public MatchResult save(MatchResult matchResult) {
        return null;
    }
}
