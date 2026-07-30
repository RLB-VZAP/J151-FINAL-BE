package com.vzap.trytons.service.catalog;

import com.vzap.trytons.dao.catalog.ClubDAO;
import com.vzap.trytons.dao.catalog.PlayerDAO;
import com.vzap.trytons.dao.catalog.PositionDAO;
import com.vzap.trytons.dto.catalog.PlayerRequestDTO;
import com.vzap.trytons.dto.catalog.PlayerResponseDTO;
import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.catalog.PlayerAvailability;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class PlayerServiceImpl implements PlayerService {
    @Inject
    private PlayerDAO playerDAO;

    @Inject
    private ClubDAO clubDAO;

    @Inject
    private PositionDAO positionDAO;

    @Override
    public PlayerResponseDTO createPlayer(PlayerRequestDTO request) {
        validatePlayerRequest(request);

        Player player = mapRequestToPlayer(request);
        player.setPlayerId(UUID.randomUUID());

        Player createdPlayer = playerDAO.createPlayer(player).orElseThrow(() -> new DataAccessException("Failed to create player.", null));

        return mapToResponse(createdPlayer);
    }

    @Override
    public PlayerResponseDTO getPlayer(UUID playerId) {
        validatePlayerId(playerId);

        Player player = playerDAO.getPlayerById(playerId).orElseThrow(() -> new ResourceNotFoundException("Player was not found."));

        PlayerResponseDTO response = mapToResponse(player);
        response.setAvailabilityStatus(statusNameOf(
                playerDAO.getCurrentAvailability(playerId).map(PlayerAvailability::getStatus).orElse(null)));

        return response;
    }

    @Override
    public List<PlayerResponseDTO> getAllPlayers() {
        return mapWithAvailability(playerDAO.getAllPlayers());
    }

    @Override
    public List<PlayerResponseDTO> searchPlayers(String playerName, UUID clubId, UUID positionId) {
        return searchPlayers(playerName, clubId, positionId, null);
    }

    @Override
    public List<PlayerResponseDTO> searchPlayers(String playerName, UUID clubId, UUID positionId, Boolean availableOnly) {
        // When availableOnly is requested (the team-selection pool), restrict to players
        // that are both on the roster (isActive) and currently ACTIVE in the availability
        // table — the same signal the squad validator enforces on submit, so the pool can
        // never offer a player the backend will reject.
        AvailabilityStatus status = Boolean.TRUE.equals(availableOnly) ? AvailabilityStatus.ACTIVE : null;
        Boolean isActive = Boolean.TRUE.equals(availableOnly) ? Boolean.TRUE : null;

        return mapWithAvailability(
                playerDAO.searchPlayers(playerName, clubId, positionId, null, null, null, null, status, isActive));
    }

    /**
     * Maps a page of players, attaching each one's current availability. The
     * statuses come from a single batch query rather than a lookup per row, which
     * matters: the catalogue is the better part of a thousand players.
     */
    private List<PlayerResponseDTO> mapWithAvailability(List<Player> players) {
        List<UUID> playerIds = new ArrayList<>(players.size());
        for (Player player : players) {
            playerIds.add(player.getPlayerId());
        }

        Map<UUID, AvailabilityStatus> statuses = playerDAO.getCurrentAvailabilityStatuses(playerIds);
        List<PlayerResponseDTO> responses = new ArrayList<>(players.size());

        for (Player player : players) {
            PlayerResponseDTO response = mapToResponse(player);
            response.setAvailabilityStatus(statusNameOf(statuses.get(player.getPlayerId())));
            responses.add(response);
        }

        return responses;
    }

    /**
     * A player with no availability record is treated as available. Records are
     * only written when something changes (an injury, a suspension), so their
     * absence is the normal state for a fit player, not missing data.
     */
    private String statusNameOf(AvailabilityStatus status) {
        return (status == null ? AvailabilityStatus.ACTIVE : status).name();
    }

    @Override
    public PlayerResponseDTO updatePlayer(UUID playerId, PlayerRequestDTO request) {
        validatePlayerId(playerId);
        validatePlayerRequest(request);

        playerDAO.getPlayerById(playerId).orElseThrow(() -> new ResourceNotFoundException("Player was not found."));

        Player player = mapRequestToPlayer(request);
        player.setPlayerId(playerId);

        Player updatedPlayer = playerDAO.updatePlayer(player).orElseThrow(() -> new DataAccessException("Failed to update player.", null));

        return mapToResponse(updatedPlayer);
    }

    private void validatePlayerRequest(PlayerRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Player details are required.");
        }

        if (request.getPlayerName() == null || request.getPlayerName().isBlank()) {
            throw new ValidationException("Player name is required.");
        }

        if (request.getValue() == null) {
            throw new ValidationException("Player value is required.");
        }

        if (request.getValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Player value cannot be negative.");
        }

        validateRating(request.getAttackingAbility(), "Attacking ability");
        validateRating(request.getDefensiveAbility(), "Defensive ability");
        validateRating(request.getKickingAbility(), "Kicking ability");
        validateRating(request.getDiscipline(), "Discipline");
        validateRating(request.getConsistency(), "Consistency");
        validateRating(request.getFitness(), "Fitness");
        validateRating(request.getCurrentForm(), "Current form");
        validateClubReference(request.getClubId());
        validatePositionReference(request.getPositionId());
    }

    private void validateRating(int rating, String fieldName) {
        if (rating < 0 || rating > 100) {
            throw new ValidationException(fieldName + " must be between 0 and 100.");
        }
    }

    private void validatePlayerId(UUID playerId) {
        if (playerId == null) {
            throw new ValidationException("Player ID is required.");
        }
    }

    private void validateClubReference(UUID clubId) {
        if (clubId == null) {
            throw new ValidationException("Club ID is required.");
        }

        clubDAO.findByClubId(clubId).orElseThrow(() -> new ResourceNotFoundException("Selected club was not found."));
    }

    private void validatePositionReference(UUID positionId) {
        if (positionId == null) {
            throw new ValidationException("Position ID is required.");
        }

        positionDAO.findById(positionId).orElseThrow(() -> new ResourceNotFoundException("Selected position was not found."));
    }

    private Player mapRequestToPlayer(PlayerRequestDTO request) {
        Player player = new Player();

        player.setClubId(request.getClubId());
        player.setPositionId(request.getPositionId());
        player.setPlayerName(request.getPlayerName().trim());
        player.setValue(request.getValue());
        player.setAttackingAbility(request.getAttackingAbility());
        player.setDefensiveAbility(request.getDefensiveAbility());
        player.setKickingAbility(request.getKickingAbility());
        player.setDiscipline(request.getDiscipline());
        player.setConsistency(request.getConsistency());
        player.setFitness(request.getFitness());
        player.setCurrentForm(request.getCurrentForm());

        return player;
    }

    private PlayerResponseDTO mapToResponse(Player player) {
        PlayerResponseDTO response = new PlayerResponseDTO();

        response.setPlayerId(player.getPlayerId());
        response.setClubId(player.getClubId());
        response.setPositionId(player.getPositionId());
        response.setPlayerName(player.getPlayerName());
        response.setValue(player.getValue());
        response.setAttackingAbility(player.getAttackingAbility());
        response.setDefensiveAbility(player.getDefensiveAbility());
        response.setKickingAbility(player.getKickingAbility());
        response.setDiscipline(player.getDiscipline());
        response.setConsistency(player.getConsistency());
        response.setFitness(player.getFitness());
        response.setCurrentForm(player.getCurrentForm());
        response.setActive(player.isActive());

        return response;
    }
}