package com.vzap.trytons.fixture.service;

import com.vzap.trytons.fixture.dto.MatchTeamScoreResponseDTO;
import com.vzap.trytons.fixture.enums.MatchTeamSide;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MatchTeamScoreServiceImpl implements MatchTeamScoreService {

    @Override
    public MatchTeamScoreResponseDTO getMatchTeamScoreById(UUID scoreId) {

        throw new UnsupportedOperationException("MatchTeamScoreServiceImpl.getMatchTeamScoreById is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after MatchTeamScoreDAO read mapping is confirmed.");
    }

    @Override
    public List<MatchTeamScoreResponseDTO> listMatchTeamScoresForResult(UUID resultId) {

        throw new UnsupportedOperationException("MatchTeamScoreServiceImpl.listMatchTeamScoresForResult is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after MatchTeamScoreDAO result-based lookup and response mapping are confirmed.");
    }

    @Override
    public MatchTeamScoreResponseDTO getMatchTeamScoreForResultSide(UUID resultId, MatchTeamSide teamSide) {

        throw new UnsupportedOperationException("MatchTeamScoreServiceImpl.getMatchTeamScoreForResultSide is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after MatchTeamScoreDAO result-side lookup and response mapping are confirmed.");
    }
}