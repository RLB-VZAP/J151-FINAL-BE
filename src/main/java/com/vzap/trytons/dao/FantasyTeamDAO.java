package com.vzap.trytons.dao;

import com.vzap.trytons.model.FantasyTeam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FantasyTeamDAO {
    Optional<FantasyTeam> createTeam(FantasyTeam team);
    Optional<FantasyTeam> getTeamById(UUID teamId);
    FantasyTeam findTeamById(UUID teamId);
    List<FantasyTeam> findTeamsByOwner(UUID ownerUserId);
    boolean updatePoints(UUID teamId, int totalPoints, int weeklyPoints);
    boolean updateLockedStatus(UUID teamId, boolean isLocked);
    boolean updateBudgetAndValue(UUID teamId, BigDecimal totalTeamValue, BigDecimal remainingBudget);
}
