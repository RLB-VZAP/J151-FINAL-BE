package com.vzap.trytons.service;

import com.vzap.trytons.dao.PlayerDAO;
import com.vzap.trytons.dto.SquadValidationResultDTO;
import com.vzap.trytons.model.Player;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;


@ApplicationScoped
public class SquadValidationServiceImpl implements SquadValidationService {
// Minimum position requirements.
    private static final int MIN_PROPS = 2;
    private static final int MIN_HOOKERS = 1;
    private static final int MIN_LOCKS = 2;
    private static final int MIN_LOOSE_FORWARDS = 3;
    private static final int MIN_SCRUM_HALVES = 1;
    private static final int MIN_FLY_HALVES = 1;
    private static final int MIN_CENTRES = 2;
    private static final int MIN_WINGS = 2;
    private static final int MIN_FULLBACKS = 1;

// Maximum position requirements.
    private static final int MAX_PROPS = 4;
    private static final int MAX_HOOKERS = 2;
    private static final int MAX_LOCKS = 4;
    private static final int MAX_LOOSE_FORWARDS = 5;
    private static final int MAX_SCRUM_HALVES = 2;
    private static final int MAX_FLY_HALVES = 2;
    private static final int MAX_CENTRES = 4;
    private static final int MAX_WINGS = 4;
    private static final int MAX_FULLBACKS = 2;

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
        List<UUID> playerIds = proposedPlayerIds;
        UUID testPlayerId;
        int duplicates = 0;
        for (UUID playerId : proposedPlayerIds) {
            testPlayerId = playerId;
            for  (UUID playerId2 : proposedPlayerIds) {
                if (testPlayerId.equals(playerId2)) {
                    duplicates++;
                }
            }
            if (duplicates > 2){
                results.addError("DUPLICATE_PLAYERS", "Duplicate player found, ID: "+testPlayerId.toString()+", number of duplicate entries: "+duplicates, "List<UUID> proposedPlayerIds");
            }
        }

    }
    /*
    Squad size
    Duplicate players
    Null or nonexistent player IDs
    Player active status
    Player availability
    Minimum position requirements
    Maximum position requirements
     */
    private void validatePositionRules(List<UUID> proposedPlayerIds, SquadValidationResultDTO results) {
        int propCount = 0;
        int hookerCount = 0;
        int lockCount = 0;
        int looseForwardCount = 0;
        int scrumHalfCount = 0;
        int flyHalfCount = 0;
        int centreCount = 0;
        int wingCount = 0;
        int fullbackCount = 0;
        int invalidCount = 0;
        for (UUID playerId : proposedPlayerIds) {
            Player player = playerDAO.getPlayerById(playerId).orElseThrow(() -> new RuntimeException("PLAYER_NOT_FOUND"));
            if (player.getPosition() == null
                    || player.getPosition().getPositionName() == null) {
                invalidCount++;
                continue;
            }

            switch (player.getPosition().getPositionName()) {
                case "Prop":
                    propCount++;
                    break;

                case "Hooker":
                    hookerCount++;
                    break;

                case "Lock":
                    lockCount++;
                    break;

                case "Loose Forward":
                    looseForwardCount++;
                    break;

                case "Scrum Half":
                    scrumHalfCount++;
                    break;

                case "Fly Half":
                    flyHalfCount++;
                    break;

                case "Centre":
                    centreCount++;
                    break;

                case "Wing":
                    wingCount++;
                    break;

                case "Fullback":
                    fullbackCount++;
                    break;

                default:
                    invalidCount++;
                    break;
            }
        }
        if (propCount < MIN_PROPS) {
            results.addError("INVALID_POSITION_COUNT", "Not enough props for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (hookerCount < MIN_HOOKERS) {
            results.addError("INVALID_POSITION_COUNT", "Not enough hookers for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (lockCount < MIN_LOCKS) {
            results.addError("INVALID_POSITION_COUNT", "Not enough locks for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (looseForwardCount < MIN_LOOSE_FORWARDS) {
            results.addError("INVALID_POSITION_COUNT", "Not enough loose forwards for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (scrumHalfCount < MIN_SCRUM_HALVES) {
            results.addError("INVALID_POSITION_COUNT", "Not enough scrum-halves for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (flyHalfCount < MIN_FLY_HALVES) {
            results.addError("INVALID_POSITION_COUNT", "Not enough fly-halves for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (centreCount < MIN_CENTRES) {
            results.addError("INVALID_POSITION_COUNT", "Not enough centres for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (wingCount < MIN_WINGS) {
            results.addError("INVALID_POSITION_COUNT", "Not enough wings for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (fullbackCount < MIN_FULLBACKS) {
            results.addError("INVALID_POSITION_COUNT", "Not enough fullbacks for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (propCount > MAX_PROPS) {
            results.addError("INVALID_POSITION_COUNT", "Too many props for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (hookerCount > MAX_HOOKERS) {
            results.addError("INVALID_POSITION_COUNT", "Too many hookers for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (lockCount > MAX_LOCKS) {
            results.addError("INVALID_POSITION_COUNT", "Too many locks for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (looseForwardCount > MAX_LOOSE_FORWARDS) {
            results.addError("INVALID_POSITION_COUNT", "Too many loose forwards for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (scrumHalfCount > MAX_SCRUM_HALVES) {
            results.addError("INVALID_POSITION_COUNT", "Too many scrum-halves for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (flyHalfCount > MAX_FLY_HALVES) {
            results.addError("INVALID_POSITION_COUNT", "Too many fly-halves for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (centreCount > MAX_CENTRES) {
            results.addError("INVALID_POSITION_COUNT", "Too many centres for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (wingCount > MAX_WINGS) {
            results.addError("INVALID_POSITION_COUNT", "Too many wings for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (fullbackCount > MAX_FULLBACKS) {
            results.addError("INVALID_POSITION_COUNT", "Too many fullbacks for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (invalidCount > 0) {
            results.addError("INVALID_PLAYER_POSITION", invalidCount + " player(s) have an invalid or missing position", "List<UUID> proposedPlayerIds");
        }
    }

    private void loadPlayers() {

    }

    private void sumPlayerValues() {
    }

    private void safeValue() {
    }
}
