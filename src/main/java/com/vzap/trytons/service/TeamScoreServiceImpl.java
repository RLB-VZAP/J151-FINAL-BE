package com.vzap.trytons.service;

import com.vzap.trytons.dto.TeamScoreUpdateResultDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class TeamScoreServiceImpl implements TeamScoreService {

    @Override
    public TeamScoreUpdateResultDTO refreshTeamScores(UUID actorUserId, UUID fixtureId) {
        return new TeamScoreUpdateResultDTO();
    }
}
