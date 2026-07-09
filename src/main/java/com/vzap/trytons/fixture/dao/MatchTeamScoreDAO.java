package com.vzap.trytons.fixture.dao;

import com.vzap.trytons.fixture.enums.MatchTeamSide;
import com.vzap.trytons.fixture.model.MatchTeamScore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchTeamScoreDAO {
    MatchTeamScore save(MatchTeamScore matchTeamScore);
    Optional<MatchTeamScore> findById(UUID ScoreId);
    List<MatchTeamScore> findByResultId(UUID resultId);
    Optional<MatchTeamScore> findByResultIdAndTeamSide(UUID resultId, MatchTeamSide teamSide);
}