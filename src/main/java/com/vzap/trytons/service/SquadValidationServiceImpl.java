package com.vzap.trytons.service;

import com.vzap.trytons.dao.PlayerDAO;
import com.vzap.trytons.dto.SquadValidationResultDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SquadValidationServiceImpl implements SquadValidationService {
    @Inject
    PlayerDAO playerDAO;

    @Override
    public <SquadValidationResult> SquadValidationResult validateSquad(List<UUID> proposedPlayerIds, BigDecimal maximumSquadValue) {
        return null;
    }

    private void validateSquadSize() {
    }

    private void validateDuplicatePlayers() {
    }

    private void validatePlayerIdsExist() {
    }

    private void validatePlayerAvailability() {
    }

    private void validatePositionRules() {
    }

    private void validateNewSquadBudget() {
    }

    private void loadPlayers() {
    }

    private void sumPlayerValues() {
    }

    private void safeValue() {
    }


}
