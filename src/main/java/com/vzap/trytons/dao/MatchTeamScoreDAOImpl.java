package com.vzap.trytons.dao;

import com.vzap.trytons.model.MatchTeamScore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MatchTeamScoreDAOImpl implements MatchTeamScoreDAO {

    @Override
    public MatchTeamScore save(MatchTeamScore matchTeamScore) {
        throw new UnsupportedOperationException("MatchTeamScoreDAOImpl stub: save is not implemented yet.");
    }

    @Override
    public Optional<MatchTeamScore> findById(UUID teamScoreId) {
        throw new UnsupportedOperationException("MatchTeamScoreDAOImpl stub: findById is not implemented yet.");
    }

    @Override
    public List<MatchTeamScore> findByResultId(UUID resultId) {
        throw new UnsupportedOperationException("MatchTeamScoreDAOImpl stub: findByResultId is not implemented yet.");
    }

    @Override
    public Optional<MatchTeamScore> findByResultIdAndTeamSide(UUID resultId, String teamSide) {
        throw new UnsupportedOperationException("MatchTeamScoreDAOImpl stub: findByResultIdAndTeamSide is not implemented yet.");
    }
}