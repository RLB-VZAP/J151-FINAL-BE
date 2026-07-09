package com.vzap.trytons.fixture.service;

import com.vzap.trytons.fixture.dto.MatchTeamScoreResponseDTO;
import com.vzap.trytons.fixture.enums.MatchTeamSide;

import java.util.List;
import java.util.UUID;

public interface MatchTeamScoreService {
    MatchTeamScoreResponseDTO getMatchTeamScoreById(UUID scoreId);
    List<MatchTeamScoreResponseDTO> listMatchTeamScoresForResult(UUID resultId);
    MatchTeamScoreResponseDTO getMatchTeamScoreForResultSide(UUID resultId, MatchTeamSide teamSide);
}