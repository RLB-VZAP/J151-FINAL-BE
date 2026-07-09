package com.vzap.trytons.fixture.dao;

import com.vzap.trytons.fixture.enums.MatchTeamSide;
import com.vzap.trytons.fixture.model.MatchTeamScore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MatchTeamScoreDAOImpl implements MatchTeamScoreDAO {

    @Override
    public MatchTeamScore save(MatchTeamScore matchTeamScore) {
        throw new UnsupportedOperationException("MatchTeamScoreDAOImpl stub: save is not implemented yet.");
    }

    @Override
    public Optional<MatchTeamScore> findById(UUID ScoreId) {
        throw new UnsupportedOperationException("MatchTeamScoreDAOImpl stub: findById is not implemented yet.");
    }

    @Override
    public List<MatchTeamScore> findByResultId(UUID resultId) {
        throw new UnsupportedOperationException("MatchTeamScoreDAOImpl stub: findByResultId is not implemented yet.");
    }

    @Override
    public Optional<MatchTeamScore> findByResultIdAndTeamSide(UUID resultId, MatchTeamSide teamSide) {
        throw new UnsupportedOperationException("MatchTeamScoreDAOImpl stub: findByResultIdAndTeamSide is not implemented yet.");
    }
}