package com.vzap.trytons.scoring.dao;

import com.vzap.trytons.scoring.model.FantasyPointBreakdown;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FantasyPointBreakdownDAO {
    FantasyPointBreakdown save(FantasyPointBreakdown fantasyPointBreakdown);
    Optional<FantasyPointBreakdown> findById(UUID breakdownId);
    List<FantasyPointBreakdown> findByPointsId(UUID pointsId);
}