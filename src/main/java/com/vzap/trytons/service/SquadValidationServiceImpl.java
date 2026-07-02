package com.vzap.trytons.service;

import com.vzap.trytons.dto.SquadValidationResult;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SquadValidationServiceImpl implements SquadValidationService {

    @Override
    public SquadValidationResult validateNewSquad(List<UUID> selectedPlayerIds) {
        return null;
    }

    @Override
    public SquadValidationResult validateTransfer(UUID teamId, UUID removedPlayerId, UUID addedPlayerId) {
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

    private void validateTransferBudget() {
    }

    private void validateTeamExists() {
    }

    private void validateRemovedPlayerIsInSquad() {
    }

    private void validateAddedPlayerIsNotAlreadyInSquad() {
    }

    private void loadPlayers() {
    }

    private void sumPlayerValues() {
    }

    private void safeValue() {
    }
}
