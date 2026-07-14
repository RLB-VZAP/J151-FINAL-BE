package com.vzap.trytons.service;

import com.vzap.trytons.dao.FantasyRoundDAO;
import com.vzap.trytons.dao.FantasyTeamDAO;
import com.vzap.trytons.dao.FantasyTeamPlayerDAO;
import com.vzap.trytons.dao.PlayerDAO;
import com.vzap.trytons.dao.TransferDAO;
import com.vzap.trytons.dto.DeadlineStatusResponseDTO;
import com.vzap.trytons.dto.LockStatusResponseDTO;
import com.vzap.trytons.dto.SquadValidationResultDTO;
import com.vzap.trytons.dto.TransferRequestDTO;
import com.vzap.trytons.dto.TransferResponseDTO;
import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.enums.TransferStatus;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.FantasyRound;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Player;
import com.vzap.trytons.model.PlayerAvailability;
import com.vzap.trytons.model.RegisteredUser;
import com.vzap.trytons.model.TeamPlayerSelection;
import com.vzap.trytons.model.Transfer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class TransferServiceImpl implements TransferService {

    private static final int FREE_TRANSFERS_PER_ROUND = 1;
    private static final int PENALTY_POINTS_PER_EXTRA_TRANSFER = 4;

    @Inject
    private TransferDAO transferDAO;

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Inject
    private FantasyTeamPlayerDAO fantasyTeamPlayerDAO;

    @Inject
    private PlayerDAO playerDAO;

    @Inject
    private FantasyRoundDAO fantasyRoundDAO;

    @Inject
    private DeadlineLockService deadlineLockService;

    @Inject
    private SquadValidationService squadValidationService;

    @Override
    @Transactional
    public TransferResponseDTO executeTransfer(String actorUserId, TransferRequestDTO request) {
        validateRequest(actorUserId, request);

        UUID actorId = parseUuid(actorUserId, "Actor user ID is invalid");
        UUID teamId = parseUuid(request.getTeamId(), "Team ID is invalid");
        UUID roundId = parseUuid(request.getRoundId(), "Round ID is invalid");
        UUID removedPlayerId = parseUuid(request.getRemovedPlayerId(), "Removed player ID is invalid");
        UUID addedPlayerId = parseUuid(request.getAddedPlayerId(), "Added player ID is invalid");

        if (removedPlayerId.equals(addedPlayerId)) {
            throw new ValidationException("The same player cannot be both added and removed in one transfer");
        }

        FantasyTeam team = fantasyTeamDAO.getTeamById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found"));

        validateTeamOwnership(actorId, team);

        FantasyRound round = fantasyRoundDAO.getRoundById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy round not found"));

        validateRoundIsOpen(round);
        enforceDeadlineAndLocks(roundId, teamId, removedPlayerId, addedPlayerId);

        if (transferDAO.existsConfirmedTransfer(teamId, roundId, removedPlayerId, addedPlayerId)) {
            throw new ConflictException("This transfer has already been confirmed for this round");
        }

        List<TeamPlayerSelection> currentSquad = fantasyTeamPlayerDAO.getSquadByTeamId(teamId);
        validateCurrentSquadRules(currentSquad, removedPlayerId, addedPlayerId);

        Player removedPlayer = playerDAO.getPlayerById(removedPlayerId)
                .orElseThrow(() -> new ResourceNotFoundException("Removed player not found"));

        Player addedPlayer = playerDAO.getPlayerById(addedPlayerId)
                .orElseThrow(() -> new ResourceNotFoundException("Added player not found"));

        validateIncomingPlayerAvailability(roundId, teamId, addedPlayer);

        BigDecimal removedValue = requirePlayerValue(removedPlayer, "Removed player");
        BigDecimal addedValue = requirePlayerValue(addedPlayer, "Added player");

        BigDecimal oldRemainingBudget = valueOrZero(team.getRemainingBudget());
        BigDecimal oldTeamValue = valueOrZero(team.getTotalTeamValue());

        BigDecimal newRemainingBudget = oldRemainingBudget.add(removedValue).subtract(addedValue);
        BigDecimal newTeamValue = oldTeamValue.subtract(removedValue).add(addedValue);

        if (newRemainingBudget.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("You cannot afford this transfer. Insufficient remaining budget.");
        }

        List<UUID> proposedPlayerIds = currentSquad.stream()
                .filter(selection -> selection != null && selection.getPlayer() != null)
                .map(selection -> selection.getPlayer().getPlayerId())
                .collect(Collectors.toList());

        proposedPlayerIds.remove(removedPlayerId);
        proposedPlayerIds.add(addedPlayerId);

        SquadValidationResultDTO validationResult = squadValidationService.validateSquad(proposedPlayerIds);

        if (validationResult == null || !validationResult.isValid()) {
            String firstError = "Unknown squad validation error";

            if (validationResult != null
                    && validationResult.getErrors() != null
                    && !validationResult.getErrors().isEmpty()) {
                firstError = validationResult.getErrors().get(0).getMessage();
            }

            throw new BusinessRuleException("Squad validation failed: " + firstError);
        }

        int transfersAlreadyThisRound = transferDAO.countConfirmedTransfers(teamId, roundId);
        boolean penaltyApplies = transfersAlreadyThisRound >= FREE_TRANSFERS_PER_ROUND;
        int penaltyPoints = penaltyApplies ? PENALTY_POINTS_PER_EXTRA_TRANSFER : 0;

        if (penaltyApplies && !request.isPenaltyConfirmed()) {
            throw new BusinessRuleException("This transfer requires penalty confirmation before it can be completed");
        }

        fantasyTeamPlayerDAO.replaceSquad(teamId, proposedPlayerIds);

        boolean budgetUpdated = fantasyTeamDAO.updateBudgetAndValue(
                teamId,
                newTeamValue,
                newRemainingBudget);

        if (!budgetUpdated) {
            throw new DataAccessException("Unable to update budget after transfer", null);
        }

        RegisteredUser createdBy = new RegisteredUser();
        createdBy.setUserId(actorId);

        Transfer transfer = new Transfer();
        transfer.setTransferId(UUID.randomUUID());
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setRound(round);
        transfer.setFantasyTeam(team);
        transfer.setRemovedPlayer(removedPlayer);
        transfer.setAddedPlayer(addedPlayer);
        transfer.setRemoved_player_value(removedValue);
        transfer.setAdded_player_value(addedValue);
        transfer.setValueDifference(addedValue.subtract(removedValue));
        transfer.setPenaltyPoints(penaltyPoints);
        transfer.setStatus(TransferStatus.CONFIRMED);
        transfer.setConfirmationDate(LocalDateTime.now());
        transfer.setCreatedBy(createdBy);

        Transfer savedTransfer = transferDAO.saveTransfer(transfer)
                .orElseThrow(() -> new DataAccessException("Unable to save transfer", null));

        return toResponse(savedTransfer, teamId);
    }

    @Override
    public List<TransferResponseDTO> listTransferHistory(String actorUserId, String teamIdValue) {
        if (isBlank(actorUserId)) {
            throw new AuthorisationException("Authentication is required");
        }

        if (isBlank(teamIdValue)) {
            throw new ValidationException("Team ID is required");
        }

        UUID actorId = parseUuid(actorUserId, "Actor user ID is invalid");
        UUID teamId = parseUuid(teamIdValue, "Team ID is invalid");

        FantasyTeam team = fantasyTeamDAO.getTeamById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found"));

        validateTeamOwnership(actorId, team);

        return transferDAO.getTransfersByTeamId(teamId).stream()
                .map(transfer -> toResponse(transfer, teamId))
                .collect(Collectors.toList());
    }

    private void validateRequest(String actorUserId, TransferRequestDTO request) {
        if (isBlank(actorUserId)) {
            throw new AuthorisationException("Authentication is required");
        }

        if (request == null) {
            throw new ValidationException("Transfer request is required");
        }

        if (isBlank(request.getTeamId())) {
            throw new ValidationException("Team ID is required");
        }

        if (isBlank(request.getRoundId())) {
            throw new ValidationException("Round ID is required");
        }

        if (isBlank(request.getRemovedPlayerId())) {
            throw new ValidationException("Removed player is required");
        }

        if (isBlank(request.getAddedPlayerId())) {
            throw new ValidationException("Added player is required");
        }
    }

    private void validateTeamOwnership(UUID actorId, FantasyTeam team) {
        if (team.getOwner() == null || team.getOwner().getUserId() == null) {
            throw new BusinessRuleException("Fantasy team owner could not be verified");
        }

        if (!team.getOwner().getUserId().equals(actorId)) {
            throw new AuthorisationException("You do not own this fantasy team");
        }

        if (Boolean.TRUE.equals(team.getIsLocked())) {
            throw new BusinessRuleException("This team is locked and cannot make transfers");
        }
    }

    private void validateRoundIsOpen(FantasyRound round) {
        if (round.getStatus() != FantasyRoundStatus.OPEN) {
            throw new BusinessRuleException("Transfers are not open for this round");
        }
    }

    private void enforceDeadlineAndLocks(
            UUID roundId,
            UUID teamId,
            UUID removedPlayerId,
            UUID addedPlayerId) {
        DeadlineStatusResponseDTO deadlineStatus = deadlineLockService.getDeadlineStatus(roundId);

        if (deadlineStatus != null) {
            if (deadlineStatus.isLocked()) {
                throw new BusinessRuleException("The transfer deadline has passed for this round");
            }

            if (!deadlineStatus.isOpenForTransfers()) {
                throw new BusinessRuleException(
                        deadlineStatus.getMessage() != null
                                ? deadlineStatus.getMessage()
                                : "Transfers are not open for this round");
            }
        }

        LockStatusResponseDTO lockStatus = deadlineLockService.getLockStatus(roundId);

        if (lockStatus != null && lockStatus.isLocked()) {
            throw new BusinessRuleException(
                    lockStatus.getMessage() != null
                            ? lockStatus.getMessage()
                            : "This round is locked for transfers");
        }

        List<UUID> lockedTeamIds = deadlineLockService.getLockedTeamIds(roundId);

        if (lockedTeamIds != null && lockedTeamIds.contains(teamId)) {
            throw new BusinessRuleException("This team is locked for the selected round");
        }

        List<UUID> lockedPlayerIds = deadlineLockService.getLockedPlayerIds(roundId, teamId);

        if (lockedPlayerIds != null && lockedPlayerIds.contains(removedPlayerId)) {
            throw new BusinessRuleException("The player you are trying to remove is locked");
        }

        if (lockedPlayerIds != null && lockedPlayerIds.contains(addedPlayerId)) {
            throw new BusinessRuleException("The player you are trying to add is locked");
        }
    }

    private void validateCurrentSquadRules(
            List<TeamPlayerSelection> currentSquad,
            UUID removedPlayerId,
            UUID addedPlayerId) {
        if (currentSquad == null) {
            throw new BusinessRuleException("Current squad could not be loaded");
        }

        boolean removedPlayerInSquad = currentSquad.stream()
                .filter(selection -> selection != null && selection.getPlayer() != null)
                .anyMatch(selection -> removedPlayerId.equals(selection.getPlayer().getPlayerId()));

        if (!removedPlayerInSquad) {
            throw new BusinessRuleException("The player you are trying to remove is not in your squad");
        }

        boolean addedPlayerAlreadyInSquad = currentSquad.stream()
                .filter(selection -> selection != null && selection.getPlayer() != null)
                .anyMatch(selection -> addedPlayerId.equals(selection.getPlayer().getPlayerId()));

        if (addedPlayerAlreadyInSquad) {
            throw new BusinessRuleException("The player you are trying to add is already in your squad");
        }
    }

    private void validateIncomingPlayerAvailability(UUID roundId, UUID teamId, Player addedPlayer) {
        if (!addedPlayer.isActive()) {
            throw new BusinessRuleException("The player you are trying to add is not active");
        }

        PlayerAvailability availability = playerDAO.getCurrentAvailability(addedPlayer.getPlayerId()).orElse(null);

        if (availability != null && availability.getStatus() != AvailabilityStatus.ACTIVE) {
            throw new BusinessRuleException("The player you are trying to add is not available");
        }

        List<Player> availablePlayers = deadlineLockService.getAvailableTransferPlayers(roundId, teamId);

        if (availablePlayers != null) {
            boolean playerAvailableForRound = availablePlayers.stream()
                    .filter(player -> player != null && player.getPlayerId() != null)
                    .anyMatch(player -> addedPlayer.getPlayerId().equals(player.getPlayerId()));

            if (!playerAvailableForRound) {
                throw new BusinessRuleException("The player you are trying to add is not available for this round");
            }
        }
    }

    private BigDecimal requirePlayerValue(Player player, String label) {
        if (player.getValue() == null) {
            throw new BusinessRuleException(label + " has no value set, so the transfer cannot be processed");
        }

        return player.getValue();
    }

    private TransferResponseDTO toResponse(Transfer transfer, UUID fallbackTeamId) {
        BigDecimal valueDifference = transfer.getValueDifference();

        if (valueDifference == null
                && transfer.getAdded_player_value() != null
                && transfer.getRemoved_player_value() != null) {
            valueDifference = transfer.getAdded_player_value().subtract(transfer.getRemoved_player_value());
        }

        return TransferResponseDTO.builder()
                .transferId(transfer.getTransferId())
                .teamId(transfer.getFantasyTeam() != null
                        ? transfer.getFantasyTeam().getTeamId()
                        : fallbackTeamId)
                .roundId(transfer.getRound() != null ? transfer.getRound().getRoundId() : null)
                .removed_player_id(transfer.getRemovedPlayer() != null
                        ? transfer.getRemovedPlayer().getPlayerId()
                        : null)
                .added_player_id(transfer.getAddedPlayer() != null
                        ? transfer.getAddedPlayer().getPlayerId()
                        : null)
                .removed_player_name(transfer.getRemovedPlayer() != null
                        ? transfer.getRemovedPlayer().getPlayerName()
                        : null)
                .added_player_name(transfer.getAddedPlayer() != null
                        ? transfer.getAddedPlayer().getPlayerName()
                        : null)
                .removed_player_value(transfer.getRemoved_player_value())
                .added_player_value(transfer.getAdded_player_value())
                .valueDifference(valueDifference)
                .penaltyPoints(transfer.getPenaltyPoints())
                .status(transfer.getStatus() != null ? transfer.getStatus().name() : null)
                .transferDate(transfer.getTransferDate())
                .confirmationDate(transfer.getConfirmationDate())
                .build();
    }

    private UUID parseUuid(String value, String message) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(message);
        }
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}