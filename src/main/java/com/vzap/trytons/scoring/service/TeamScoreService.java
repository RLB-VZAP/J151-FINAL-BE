package com.vzap.trytons.scoring.service;

import com.vzap.trytons.scoring.dto.TeamScoreUpdateResultDTO;

import java.util.UUID;

public interface TeamScoreService {
    TeamScoreUpdateResultDTO refreshTeamScores(UUID actorUserId, UUID fixtureId);
}