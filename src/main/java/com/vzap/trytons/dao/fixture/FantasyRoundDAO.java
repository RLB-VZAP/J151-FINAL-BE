package com.vzap.trytons.dao.fixture;

import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.model.fixture.FantasyRound;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FantasyRoundDAO {
    Optional<FantasyRound> getRoundById(UUID roundId);
    Optional<FantasyRound> getRoundBySeasonAndNumber(String season, int roundNumber);
    List<FantasyRound> getAllRounds();
    List<FantasyRound> getRoundsByStatus(FantasyRoundStatus status);
    Optional<FantasyRound> getCurrentOpenRound();
    boolean updateRoundStatus(UUID roundId, FantasyRoundStatus status);
    boolean roundExists(UUID roundId);

    FantasyRound createRound(FantasyRound round);
    /** Highest roundNumber used in a season, or 0 when the season has no rounds yet. */
    int getMaxRoundNumber(String season);
}