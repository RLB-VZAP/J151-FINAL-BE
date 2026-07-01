package com.vzap.trytons.dao;

import com.vzap.trytons.model.FantasyTeam;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

public interface FantasyTeamDAO {
    public void createTeam(FantasyTeam team);
    public FantasyTeam findTeamById(UUID teamId);
    public List<FantasyTeam> findTeamsByOwner(UUID ownerId);
    public void updatePoints(UUID teamId, int totalPoints, int weeklyPoints);
    public void updateLockedStatus(UUID teamId, boolean isLocked);
    public void updateBudget(UUID teamId, BigDecimal totalTeamValue, BigDecimal remainingBudget);
}
