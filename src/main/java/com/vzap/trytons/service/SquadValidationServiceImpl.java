package com.vzap.trytons.service;

import com.vzap.trytons.dao.PlayerDAO;
import com.vzap.trytons.dto.SquadValidationResultDTO;
import com.vzap.trytons.model.Player;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
/*
Squad size
Duplicate players
Null or nonexistent player IDs
Player active status
Player availability
Minimum position requirements
Maximum position requirements
 */
@ApplicationScoped
public class SquadValidationServiceImpl implements SquadValidationService {
    @Inject
    PlayerDAO playerDAO;

    @Override
    public SquadValidationResultDTO validateSquad(List<UUID> proposedPlayerIds) {
        SquadValidationResultDTO results;

        return null;
    }

    private void validateSquadSize(List<UUID> proposedPlayerIds, SquadValidationResultDTO results) {
        int size = proposedPlayerIds.size();
        if (size != 20){
            results.addError("INVALID_SQUAD_SIZE", "Squad size must be 20", "List<UUID> proposedPlayerIds");
        }
    }

    private void validateDuplicatePlayers(List<UUID> proposedPlayerIds,SquadValidationResultDTO results) {
        int found = 0;
        for (UUID playerId : proposedPlayerIds) {
            for (UUID playerId2 : proposedPlayerIds) {
                if (playerId.equals(playerId2)) {
                    found++;
                }
            }
        }
        if (found >1){
            results.addError("DUPLICATE_PLAYERS", "Duplicate player found", "List<UUID> proposedPlayerIds");
        }
    }

    private void validatePlayerIdsExist(List<UUID> proposedPlayerIds,SquadValidationResultDTO results) {
        try {

            for (UUID playerId : proposedPlayerIds) {
                playerDAO.getPlayerById(playerId).orElseThrow(() -> new RuntimeException("PLAYER_NOT_FOUND"));
            }
        }catch (RuntimeException e){
            results.addError(e.getMessage(), "Player not found", "List<UUID> proposedPlayerIds");
        }
    }

    private void validatePlayerAvailability(List<UUID> proposedPlayerIds,SquadValidationResultDTO results) {
        for (UUID playerId : proposedPlayerIds) {
            Player present = playerDAO.getPlayerById(playerId).orElseThrow(() -> new RuntimeException("PLAYER_NOT_FOUND"));
            if (present) {

            }
        }
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
