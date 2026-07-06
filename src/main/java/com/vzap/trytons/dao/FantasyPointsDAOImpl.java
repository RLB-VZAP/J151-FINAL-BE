package com.vzap.trytons.dao;

import com.vzap.trytons.model.FantasyPoints;

import java.util.List;
import java.util.UUID;

public class FantasyPointsDAOImpl implements FantasyPointsDAO {
    @Override
    public FantasyPoints save(FantasyPoints points) {
        return null;
    }

    @Override
    public List<FantasyPoints> findByFixtureId(UUID fixtureId) {
        return List.of();
    }

    @Override
    public List<FantasyPoints> findByTeamId(UUID teamId) {
        return List.of();
    }
}
