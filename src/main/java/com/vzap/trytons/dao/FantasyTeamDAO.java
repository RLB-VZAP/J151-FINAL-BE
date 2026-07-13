package com.vzap.trytons.dao;

import com.vzap.trytons.model.FantasyTeam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface
FantasyTeamDAO {
    public Optional<FantasyTeam> createTeam(FantasyTeam team);
    public Optional<FantasyTeam> getTeamById(UUID teamId);
    public FantasyTeam findTeamById(UUID teamId);
    public boolean updatePoints(UUID teamId, int totalPoints, int weeklyPoints);
    public boolean updateLockedStatus(UUID teamId, boolean isLocked);
    public boolean updateBudgetAndValue(UUID teamId, BigDecimal totalTeamValue, BigDecimal remainingBudget);
}