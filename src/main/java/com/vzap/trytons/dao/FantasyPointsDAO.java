package com.vzap.trytons.dao;

import com.vzap.trytons.model.FantasyPoints;

import java.util.List;
import java.util.UUID;

public interface FantasyPointsDAO {
    FantasyPoints save(FantasyPoints points);
    List<FantasyPoints> findByFixtureId(UUID fixtureId);
    List<FantasyPoints> findByTeamId(UUID teamId);
}
