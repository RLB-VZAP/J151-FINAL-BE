package com.vzap.trytons.scoring.dao;

import com.vzap.trytons.scoring.model.FantasyPointBreakdown;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FantasyPointBreakdownDAOImpl implements FantasyPointBreakdownDAO {

    @Override
    public FantasyPointBreakdown save(FantasyPointBreakdown fantasyPointBreakdown) {
        throw new UnsupportedOperationException("FantasyPointBreakdownDAOImpl stub: save is not implemented yet.");
    }

    @Override
    public Optional<FantasyPointBreakdown> findById(UUID breakdownId) {
        throw new UnsupportedOperationException("FantasyPointBreakdownDAOImpl stub: findById is not implemented yet.");
    }

    @Override
    public List<FantasyPointBreakdown> findByPointsId(UUID pointsId) {
        throw new UnsupportedOperationException("FantasyPointBreakdownDAOImpl stub: findByPointsId is not implemented yet.");
    }
}