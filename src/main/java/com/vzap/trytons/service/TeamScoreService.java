package com.vzap.trytons.service;

import com.vzap.trytons.dto.TeamScoreUpdateResultDTO;

import java.util.UUID;

public interface TeamScoreService {
    TeamScoreUpdateResultDTO updateTeamScoresForFixture(String fixtureId);
    TeamScoreUpdateResultDTO recalculateTeamTotals(String teamId, String season);
}