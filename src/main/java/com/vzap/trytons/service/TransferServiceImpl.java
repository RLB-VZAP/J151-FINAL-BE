package com.vzap.trytons.service;

import com.vzap.trytons.dao.FantasyTeamDAO;
import com.vzap.trytons.dao.FantasyTeamPlayerDAO;
import com.vzap.trytons.dao.FixtureDAO;
import com.vzap.trytons.dao.PlayerDAO;
import com.vzap.trytons.dao.TransferDAO;
import com.vzap.trytons.dao.TransferHistoryDAO;
import com.vzap.trytons.dto.DeadlineStatusResponseDTO;
import com.vzap.trytons.dto.LockStatusResponseDTO;
import com.vzap.trytons.dto.SquadValidationResultDTO;
import com.vzap.trytons.dto.TransferRequestDTO;
import com.vzap.trytons.dto.TransferResponseDTO;
import com.vzap.trytons.enums.TransferWindowStatus;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Fixture;
import com.vzap.trytons.model.Player;
import com.vzap.trytons.model.TeamPlayerSelection;
import com.vzap.trytons.model.Transfer;
import com.vzap.trytons.model.TransferHistory;
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
    private TransferHistoryDAO transferHistoryDAO;

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Inject
    private FantasyTeamPlayerDAO fantasyTeamPlayerDAO;

    @Inject
    private PlayerDAO playerDAO;

    @Inject
    private FixtureDAO fixtureDAO;

    @Inject
    private DeadlineLockService deadlineLockService;

    @Inject
    private SquadValidationService squadValidationService;

    @Override
    @Transactional
    public TransferResponseDTO executeTransfer(UUID authenticatedActorId, TransferRequestDTO request) {
        validateRequest(authenticatedActorId, request);

        FantasyTeam team = fantasyTeamDAO.getTeamById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found"));

        validateTeamOwnership(authenticatedActorId, team);

        Fixture fixture = fixtureDAO.findFixtureById(request.getFixtureId());
        if (fixture == null) {
            throw new ResourceNotFoundException("Fixture not found");
        }

        int roundNumber = fixture.getMatchRoundNumber();

        enforceDeadlineAndLocks(request, team);

        validateDuplicateAndConflictChecks(request, roundNumber);

        List<TeamPlayerSelection> currentSquad = fantasyTeamPlayerDAO.getSquadByTeamId(request.getTeamId());

        validateCurrentSquadRules(request, currentSquad);

        Player removedPlayer = findPlayerIfPresent(request.getRemovedPlayerId(), "Removed player not found");
        Player addedPlayer = findPlayerIfPresent(request.getAddedPlayerId(), "Added player not found");

        if (addedPlayer != null && !addedPlayer.isActive()) {
            throw new BusinessRuleException("The player you are trying to add is not active");
        }

        BigDecimal oldRemainingBudget = valueOrZero(team.getRemainingBudget());
        BigDecimal oldTeamValue = valueOrZero(team.getTotalTeamValue());

        BigDecimal removedValue = removedPlayer != null ? requirePlayerValue(removedPlayer, "Removed player") : BigDecimal.ZERO;
        BigDecimal addedValue = addedPlayer != null ? requirePlayerValue(addedPlayer, "Added player") : BigDecimal.ZERO;

        BigDecimal newRemainingBudget = oldRemainingBudget.add(removedValue).subtract(addedValue);
        BigDecimal newTeamValue = oldTeamValue.subtract(removedValue).add(addedValue);

        if (newRemainingBudget.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("You cannot afford this transfer. Insufficient remaining budget.");
        }

        List<UUID> proposedPlayerIds = currentSquad.stream()
                .map(selection -> selection.getPlayer().getPlayerId())
                .collect(Collectors.toList());

        if (request.getRemovedPlayerId() != null) {
            proposedPlayerIds.remove(request.getRemovedPlayerId());
        }

        if (request.getAddedPlayerId() != null) {
            proposedPlayerIds.add(request.getAddedPlayerId());
        }

        SquadValidationResultDTO validationResult = squadValidationService.validateSquad(proposedPlayerIds, newTeamValue);

        if (!validationResult.isValid()) {
            String firstError = validationResult.getErrors().isEmpty()
                    ? "Unknown squad validation error"
                    : validationResult.getErrors().get(0).getMessage();

            throw new BusinessRuleException("Squad validation failed: " + firstError);
        }

        int transfersAlreadyThisRound = transferDAO.countTransfersForTeamInRound(team.getTeamId(), roundNumber);
        boolean penaltyApplied = transfersAlreadyThisRound >= FREE_TRANSFERS_PER_ROUND;
        int penaltyPoints = penaltyApplied ? PENALTY_POINTS_PER_EXTRA_TRANSFER : 0;

        fantasyTeamPlayerDAO.replaceSquad(request.getTeamId(), proposedPlayerIds);

        boolean budgetUpdated = fantasyTeamDAO.updateBudgetAndValue(
                team.getTeamId(),
                newTeamValue,
                newRemainingBudget);

        if (!budgetUpdated) {
            throw new DataAccessException("Unable to update budget after transfer", null);
        }

        Transfer transfer = new Transfer();
        transfer.setTransferId(UUID.randomUUID());
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setPenaltyApplied(penaltyApplied);
        transfer.setPenaltyPoints(penaltyPoints);
        transfer.setTransferWindowStatus(TransferWindowStatus.OPEN);
        transfer.setRoundNumber(roundNumber);
        transfer.setConfirmed(true);
        transfer.setFantasyTeam(team);
        transfer.setRemovedPlayer(removedPlayer);
        transfer.setAddedPlayer(addedPlayer);

        transferDAO.saveTransfer(transfer)
                .orElseThrow(() -> new DataAccessException("Unable to save transfer", null));

        TransferHistory history = new TransferHistory();
        history.setTransferHistoryId(UUID.randomUUID());
        history.setOldTeamValue(oldTeamValue);
        history.setNewTeamValue(newTeamValue);
        history.setOldRemainingBudget(oldRemainingBudget);
        history.setNewRemainingBudget(newRemainingBudget);
        history.setPenaltyPoints(penaltyPoints);
        history.setCreatedAt(LocalDateTime.now());
        history.setTransfer(transfer);
        history.setFantasyTeam(team);
        history.setRemovedPlayer(removedPlayer);
        history.setAddedPlayer(addedPlayer);

        transferHistoryDAO.saveTransferHistory(history)
                .orElseThrow(() -> new DataAccessException("Unable to save transfer history", null));

        return toResponse(transfer, newRemainingBudget, newTeamValue);
    }

    @Override
    public List<TransferResponseDTO> listTransferHistory(UUID authenticatedActorId, UUID teamId) {
        if (authenticatedActorId == null) {
            throw new AuthorisationException("Authentication is required");
        }

        if (teamId == null) {
            throw new ValidationException("Team ID is required");
        }

        FantasyTeam team = fantasyTeamDAO.getTeamById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found"));

        validateTeamOwnership(authenticatedActorId, team);

        return transferDAO.findHistoryForTeam(teamId).stream()
                .map(transfer -> toResponse(transfer, null, null))
                .collect(Collectors.toList());
    }

    private void validateRequest(UUID authenticatedActorId, TransferRequestDTO request) {
        if (authenticatedActorId == null) {
            throw new AuthorisationException("Authentication is required");
        }

        if (request == null) {
            throw new ValidationException("Transfer request is required");
        }

        if (request.getTeamId() == null) {
            throw new ValidationException("Team ID is required");
        }

        if (request.getFixtureId() == null) {
            throw new ValidationException("Fixture ID is required");
        }

        if (request.getRemovedPlayerId() == null && request.getAddedPlayerId() == null) {
            throw new ValidationException("A transfer must include a player to remove and/or add");
        }

        if (request.getRemovedPlayerId() != null
                && request.getAddedPlayerId() != null
                && request.getRemovedPlayerId().equals(request.getAddedPlayerId())) {
            throw new ValidationException("The same player cannot be both added and removed in one transfer");
        }
    }

    private void validateTeamOwnership(UUID authenticatedActorId, FantasyTeam team) {
        if (team.getOwner() == null || team.getOwner().getUserId() == null) {
            throw new BusinessRuleException("Fantasy team owner could not be verified");
        }

        if (!team.getOwner().getUserId().equals(authenticatedActorId)) {
            throw new AuthorisationException("You do not own this fantasy team");
        }

        if (Boolean.TRUE.equals(team.getIsLocked())) {
            throw new BusinessRuleException("This team cannot be transferred while locked");
        }
    }

    private void enforceDeadlineAndLocks(TransferRequestDTO request, FantasyTeam team) {
        DeadlineStatusResponseDTO deadlineStatus = deadlineLockService.getDeadlineStatus(request.getFixtureId());

        if (deadlineStatus != null && deadlineStatus.isLocked()) {
            throw new BusinessRuleException("The transfer deadline has passed for this fixture");
        }

        LockStatusResponseDTO lockStatus = deadlineLockService.getLockStatus(request.getFixtureId());

        if (lockStatus != null && lockStatus.isLocked()) {
            throw new BusinessRuleException(
                    lockStatus.getMessage() != null
                            ? lockStatus.getMessage()
                            : "Transfers are locked for this fixture"
            );
        }

        List<UUID> lockedTeamIds = deadlineLockService.getLockedTeamIds(request.getFixtureId());
        if (lockedTeamIds != null && lockedTeamIds.contains(team.getTeamId())) {
            throw new BusinessRuleException("This team is locked for the selected fixture");
        }

        List<UUID> lockedPlayerIds = deadlineLockService.getLockedPlayerIds(request.getFixtureId());

        if (lockedPlayerIds != null && request.getRemovedPlayerId() != null
                && lockedPlayerIds.contains(request.getRemovedPlayerId())) {
            throw new BusinessRuleException("The player you are trying to remove is locked");
        }

        if (lockedPlayerIds != null && request.getAddedPlayerId() != null
                && lockedPlayerIds.contains(request.getAddedPlayerId())) {
            throw new BusinessRuleException("The player you are trying to add is locked");
        }
    }

    private void validateDuplicateAndConflictChecks(TransferRequestDTO request, int roundNumber) {
        boolean duplicateTransfer = transferDAO.existsDuplicateTransfer(
                request.getTeamId(),
                request.getRemovedPlayerId(),
                request.getAddedPlayerId(),
                roundNumber);

        if (duplicateTransfer) {
            throw new ConflictException("This transfer has already been made for this round");
        }

        if (request.getRemovedPlayerId() != null
                && transferDAO.existsPlayerConflict(request.getTeamId(), request.getRemovedPlayerId(), roundNumber)) {
            throw new ConflictException("The removed player is already involved in another transfer this round");
        }

        if (request.getAddedPlayerId() != null
                && transferDAO.existsPlayerConflict(request.getTeamId(), request.getAddedPlayerId(), roundNumber)) {
            throw new ConflictException("The added player is already involved in another transfer this round");
        }
    }

    private void validateCurrentSquadRules(TransferRequestDTO request, List<TeamPlayerSelection> currentSquad) {
        if (request.getRemovedPlayerId() != null) {
            boolean inSquad = currentSquad.stream()
                    .anyMatch(selection -> selection.getPlayer().getPlayerId().equals(request.getRemovedPlayerId()));

            if (!inSquad) {
                throw new BusinessRuleException("The player you are trying to remove is not in your squad");
            }
        }

        if (request.getAddedPlayerId() != null) {
            boolean alreadyInSquad = currentSquad.stream()
                    .anyMatch(selection -> selection.getPlayer().getPlayerId().equals(request.getAddedPlayerId()));

            if (alreadyInSquad) {
                throw new BusinessRuleException("The player you are trying to add is already in your squad");
            }
        }
    }

    private Player findPlayerIfPresent(UUID playerId, String notFoundMessage) {
        if (playerId == null) {
            return null;
        }

        return playerDAO.getPlayerById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMessage));
    }

    private BigDecimal requirePlayerValue(Player player, String label) {
        if (player.getValue() == null) {
            throw new BusinessRuleException(label + " has no value set, so the transfer cannot be processed");
        }

        return player.getValue();
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private TransferResponseDTO toResponse(
            Transfer transfer,
            BigDecimal newRemainingBudget,
            BigDecimal newTeamValue) {
        return new TransferResponseDTO(
                transfer.getTransferId(),
                transfer.getFantasyTeam() != null ? transfer.getFantasyTeam().getTeamId() : null,
                transfer.getRemovedPlayer() != null ? transfer.getRemovedPlayer().getPlayerId() : null,
                transfer.getAddedPlayer() != null ? transfer.getAddedPlayer().getPlayerId() : null,
                transfer.getTransferDate(),
                Boolean.TRUE.equals(transfer.getPenaltyApplied()),
                transfer.getPenaltyPoints(),
                newRemainingBudget,
                newTeamValue);
    }
}