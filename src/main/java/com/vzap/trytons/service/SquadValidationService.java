package com.vzap.trytons.service;

import com.vzap.trytons.dto.SquadValidationResult;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Player;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SquadValidationService {
    SquadValidationResult validateNewSquad(List<UUID> selectedPlayerIds);
    SquadValidationResult validateTransfer(UUID teamId, UUID removedPlayerId, UUID addedPlayerId);
    void validateSquadSize(List<Player> players);
    void validateDuplicatePlayers(List<UUID> selectedPlayerIds);
    void validatePlayerIdsExist(List<UUID> selectedPlayerIds, List<Player> foundPlayers);
    void validatePlayerAvailability(List<Player> players);
    void validatePositionRules(List<Player> players);
    void validateBudget(List<Player> players);
    void validateTeamExists(FantasyTeam team);
    void validateRemovedPlayerIsInSquad(List<Player> currentSquad, UUID removedPlayerId);
    void validateAddedPlayerIsNotAlreadyInSquad(List<Player> currentSquad, UUID addedPlayerId);
    void validatePlayerIsAvailable(Player player);
    void validateBudget(FantasyTeam team, BigDecimal addedValue, BigDecimal removedValue);
}
