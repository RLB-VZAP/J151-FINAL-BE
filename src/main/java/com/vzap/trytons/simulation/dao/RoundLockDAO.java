package com.vzap.trytons.simulation.dao;

import com.vzap.trytons.simulation.enums.RoundLockAction;
import com.vzap.trytons.simulation.model.RoundLock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoundLockDAO {
    Optional<RoundLock> createRoundLock(RoundLock roundLock);
    Optional<RoundLock> getRoundLockById(UUID lockId);
    List<RoundLock> getRoundLocksByRoundId(UUID roundId);
    Optional<RoundLock> getLatestRoundLockByRoundId(UUID roundId);
    List<RoundLock> getRoundLocksByAction(RoundLockAction lockAction);
}