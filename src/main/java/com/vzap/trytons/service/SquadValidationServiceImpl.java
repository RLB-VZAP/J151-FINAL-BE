package com.vzap.trytons.service;

import com.vzap.trytons.dao.PlayerDAO;
import com.vzap.trytons.dto.SquadValidationResultDTO;
import com.vzap.trytons.exceptions.ValidationException;
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
    public SquadValidationResultDTO validateSquad(List<UUID> proposedPlayerIds, BigDecimal maximumSquadValue) {
        SquadValidationResultDTO results;

        return null;
    }

    private void validateSquadSize(List<UUID> proposedPlayerIds, SquadValidationResultDTO results) {
        int size = proposedPlayerIds.size();
        if (size != 20){
            throw new ValidationException("Squad size is not valid. Squad size must be 20.");
        }
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
