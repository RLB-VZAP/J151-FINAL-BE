package com.vzap.trytons.service;

import com.vzap.trytons.dao.ClubDAO;
import com.vzap.trytons.dao.PlayerDAO;
import com.vzap.trytons.dao.PositionDAO;
import com.vzap.trytons.dto.PlayerRequestDTO;
import com.vzap.trytons.dto.PlayerResponseDTO;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.Club;
import com.vzap.trytons.model.Player;
import com.vzap.trytons.model.Position;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

        /*
         * Fantasy points are calculated by match-processing logic,
         * not supplied by an admin player request.
         */
        player.setTotalFantasyPoints(0);

        /*
         * A new player is active by default.
         * Deactivation is handled through the player deactivation workflow.
         */
        player.setActive(true);

        Player createdPlayer = playerDAO.createPlayer(player).orElseThrow(() -> new DataAccessException("Failed to create player.", null));
        return mapToResponse(createdPlayer);
    }

    @Override
    public PlayerResponseDTO getPlayer(UUID playerId) {
        validatePlayerId(playerId);

        Player player = playerDAO.getPlayerById(playerId).orElseThrow(() -> new ResourceNotFoundException("Player was not found."));

        return mapToResponse(player);
    }

    @Override
    public List<PlayerResponseDTO> getAllPlayers() {
        List<Player> players = playerDAO.getAllPlayers();
        List<PlayerResponseDTO> responses = new ArrayList<>();

        for (Player player : players) {
            responses.add(mapToResponse(player));
        }
        return responses;
    }

    @Override
    public List<PlayerResponseDTO> searchPlayers(String playerName, UUID clubId, UUID positionId) {
        List<Player> players = playerDAO.searchPlayers(
                playerName,
                clubId,
                positionId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        List<PlayerResponseDTO> responses = new ArrayList<>();

        for (Player player : players) {
            responses.add(mapToResponse(player));
        }
        return responses;
    }

    @Override
    public PlayerResponseDTO updatePlayer(UUID playerId, PlayerRequestDTO request) {
        validatePlayerId(playerId);
        validatePlayerRequest(request);

        Player existingPlayer = playerDAO.getPlayerById(playerId).orElseThrow(() -> new ResourceNotFoundException("Player was not found."));

        Player player = mapRequestToPlayer(request);
        player.setPlayerId(playerId);

        /*
         * These fields are not edited through ordinary player maintenance.
         * Fantasy points are calculated elsewhere and status changes use
         * the dedicated activation/deactivation workflow.
         */
        player.setTotalFantasyPoints(existingPlayer.getTotalFantasyPoints());
        player.setActive(existingPlayer.isActive());

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

        if (request.getTotalFantasyPoints() < 0) {
            throw new ValidationException("Total fantasy points cannot be negative.");
        }

        validateClubReference(request);
        validatePositionReference(request);
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

    private void validateClubReference(PlayerRequestDTO request) {
        if (request.getClub() == null || request.getClub().getClubId() == null) {
            throw new ValidationException("A valid club reference is required.");
        }
    }

    private void validatePositionReference(PlayerRequestDTO request) {
        if (request.getPosition() == null || request.getPosition().getPositionId() == null) {
            throw new ValidationException("A valid position reference is required.");
        }
    }

    private Player mapRequestToPlayer(PlayerRequestDTO request) {

        Club club = clubDAO.findByClubId(request.getClub().getClubId()).orElseThrow(() -> new ResourceNotFoundException("Selected club was not found."));
        Position position = positionDAO.findById(request.getPosition().getPositionId()).orElseThrow(() -> new ResourceNotFoundException("Selected position was not found."));

        Player player = new Player();
        player.setPlayerName(request.getPlayerName().trim());
        player.setValue(request.getValue());
        player.setAttackingAbility(request.getAttackingAbility());
        player.setDefensiveAbility(request.getDefensiveAbility());
        player.setKickingAbility(request.getKickingAbility());
        player.setDiscipline(request.getDiscipline());
        player.setConsistency(request.getConsistency());
        player.setFitness(request.getFitness());
        player.setCurrentForm(request.getCurrentForm());
        player.setClub(club);
        player.setPosition(position);

        return player;
    }

    private PlayerResponseDTO mapToResponse(Player player) {
        PlayerResponseDTO response = new PlayerResponseDTO();

        response.setPlayerId(player.getPlayerId());
        response.setPlayerName(player.getPlayerName());
        response.setValue(player.getValue());
        response.setAttackingAbility(player.getAttackingAbility());
        response.setDefensiveAbility(player.getDefensiveAbility());
        response.setKickingAbility(player.getKickingAbility());
        response.setDiscipline(player.getDiscipline());
        response.setConsistency(player.getConsistency());
        response.setFitness(player.getFitness());
        response.setCurrentForm(player.getCurrentForm());
        response.setTotalFantasyPoints(player.getTotalFantasyPoints());
        response.setActive(player.isActive());
        response.setClub(player.getClub());
        response.setPosition(player.getPosition());

        return response;
    }
}