package com.vzap.trytons.dao;

import com.vzap.trytons.enums.RoundLockAction;
import com.vzap.trytons.model.RoundLock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RoundLockDAOImpl implements RoundLockDAO {
    @Override
    public Optional<RoundLock> createRoundLock(RoundLock roundLock) {
        return Optional.empty();
    }

    @Override
    public Optional<RoundLock> getRoundLockById(UUID lockId) {
        return Optional.empty();
    }

    @Override
    public List<RoundLock> getRoundLocksByRoundId(UUID roundId) {
        return List.of();
    }

    @Override
    public Optional<RoundLock> getLatestRoundLockByRoundId(UUID roundId) {
        return Optional.empty();
    }

    @Override
    public List<RoundLock> getRoundLocksByAction(RoundLockAction lockAction) {
        return List.of();
    }
}
