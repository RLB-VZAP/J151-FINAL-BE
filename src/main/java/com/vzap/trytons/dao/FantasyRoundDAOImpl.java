package com.vzap.trytons.dao;

import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.model.FantasyRound;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FantasyRoundDAOImpl implements FantasyRoundDAO {

    @Override
    public Optional<FantasyRound> getRoundById(UUID roundId) {
        return Optional.empty();
    }

    @Override
    public Optional<FantasyRound> getRoundBySeasonAndNumber(String season, int roundNumber) {
        return Optional.empty();
    }

    @Override
    public List<FantasyRound> getAllRounds() {
        return List.of();
    }

    @Override
    public List<FantasyRound> getRoundsByStatus(FantasyRoundStatus status) {
        return List.of();
    }

    @Override
    public Optional<FantasyRound> getCurrentOpenRound() {
        return Optional.empty();
    }

    @Override
    public boolean updateRoundStatus(UUID roundId, FantasyRoundStatus status) {
        return false;
    }

    @Override
    public boolean roundExists(UUID roundId) {
        return false;
    }
}
