package com.vzap.trytons.service;

import com.vzap.trytons.dto.TeamScoreUpdateResultDTO;

import java.util.UUID;

public interface TeamScoreService {
    TeamScoreUpdateResultDTO refreshTeamScores(UUID actorUserId, UUID fixtureId);
}