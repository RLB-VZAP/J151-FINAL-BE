package com.vzap.trytons.service;

import com.vzap.trytons.dao.*;
import com.vzap.trytons.dto.SquadValidationResultDTO;
import com.vzap.trytons.dto.TransferRequestDTO;
import com.vzap.trytons.dto.TransferResponseDTO;
import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.enums.TransferStatus;
import com.vzap.trytons.enums.TransferWindowStatus;
import com.vzap.trytons.exceptions.*;
import com.vzap.trytons.model.*;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TransferServiceImpl implements TransferService {

    //It's stored in only one place in the class at the very top
    //to make it easy for admins to change, since only they have such permissions.
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
    private SquadValidationService squadValidationService;
    @Inject
    private FantasyRoundDAO fantasyRoundDAO;

    @Override
    @Transactional
    public TransferResponseDTO executeTransfer(UUID requestingUserId, TransferRequestDTO request) {
        if (request.getRemovedPlayerId() == null || request.getAddedPlayerId() == null) {
            throw new ValidationException("A transfer must include both a player to remove and a player to add");
        }
        if (request.getRemovedPlayerId().equals(request.getAddedPlayerId())) {
            throw new ValidationException("The same player cannot be both added and removed in one transfer.");
        }

        FantasyTeam team = fantasyTeamDAO.getTeamById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found"));

        if (!team.getOwner().getUserId().equals(requestingUserId)) {
            throw new AuthorisationException("You do not own this fantasy team");
        }

        FantasyRound currentRound = fantasyRoundDAO.getCurrentOpenRound()
                .orElseThrow(() -> new BusinessRuleException("There is no open round to transfer in right now"));

        if (currentRound.getStatus() != FantasyRoundStatus.OPEN) {
            throw new BusinessRuleException("Transfers are closed for the current round");
        }

        List<TeamPlayerSelection> currentSquad = fantasyTeamPlayerDAO.getSquadByTeamId(request.getTeamId());

        if (request.getRemovedPlayerId() != null) {
            boolean inSquad = currentSquad.stream()
                    .anyMatch(s -> s.getPlayer().getPlayerId().equals(request.getRemovedPlayerId()));
            if (!inSquad) {
                throw new BusinessRuleException("The player you are trying to remove is not in your squad.");
            }
        }

        if (request.getAddedPlayerId() != null) {
            boolean alreadyInSquad = currentSquad.stream()
                    .anyMatch(s -> s.getPlayer().getPlayerId().equals(request.getAddedPlayerId()));
            if (alreadyInSquad) {
                throw new BusinessRuleException("The player you are trying to add is already in your squad.");
            }
        }

        Player addedPlayer = null;
        if (request.getAddedPlayerId() != null) {
            addedPlayer = playerDAO.getPlayerById(request.getAddedPlayerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Player not found"));
        }

        Player removedPlayer = null;
        if (request.getRemovedPlayerId() != null) {
            removedPlayer = playerDAO.getPlayerById(request.getRemovedPlayerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Player not found"));
        }

        BigDecimal removedValue = removedPlayer != null ? removedPlayer.getValue() : BigDecimal.ZERO;
        BigDecimal addedValue = addedPlayer != null ? addedPlayer.getValue() : BigDecimal.ZERO;

        if (removedValue == null) {
            throw new BusinessRuleException("Removed player has no value set — cannot process transfer.");
        }
        if (addedPlayer != null && addedValue == null) {
            throw new BusinessRuleException("Added player has no value set — cannot process transfer.");
        }

        BigDecimal oldRemainingBudget = team.getRemainingBudget();
        BigDecimal oldTeamValue = team.getTotalTeamValue();
        BigDecimal newRemainingBudget = oldRemainingBudget.add(removedValue).subtract(addedValue);
        BigDecimal newTeamValue = oldTeamValue.subtract(removedValue).add(addedValue);

        if (newRemainingBudget.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("You cannot afford this transfer. Insufficient remaining budget.");
        }

        List<UUID> proposedPlayerIds = currentSquad.stream()
                .map(s -> s.getPlayer().getPlayerId())
                .collect(Collectors.toList());

        if (request.getRemovedPlayerId() != null) {
            proposedPlayerIds.remove(request.getRemovedPlayerId());
        }
        if (request.getAddedPlayerId() != null) {
            proposedPlayerIds.add(request.getAddedPlayerId());
        }

        SquadValidationResultDTO validationResult = squadValidationService.validateSquad(
                proposedPlayerIds);

        if (!validationResult.isValid()) {
            String firstError = validationResult.getErrors().get(0).getMessage();
            throw new BusinessRuleException("Squad validation failed: " + firstError);
        }

        int transfersAlreadyThisRound = transferDAO.countConfirmedTransfers(team.getTeamId(), currentRound.getRoundId());
        boolean penaltyApplied = transfersAlreadyThisRound >= FREE_TRANSFERS_PER_ROUND;
        int penaltyPoints = penaltyApplied ? PENALTY_POINTS_PER_EXTRA_TRANSFER : 0;

        fantasyTeamPlayerDAO.replaceSquad(request.getTeamId(), proposedPlayerIds);

        boolean budgetUpdated = fantasyTeamDAO.updateBudgetAndValue(team.getTeamId(), newRemainingBudget, newTeamValue);
        if (!budgetUpdated) {
            throw new DataAccessException("Unable to update budget after transfer", null);
        }

        RegisteredUser createdBy = new RegisteredUser();
        createdBy.setUserId(requestingUserId);

        Transfer transfer = new Transfer();
        transfer.setTransferId(UUID.randomUUID());
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setRound(currentRound);
        transfer.setFantasyTeam(team);
        transfer.setRemovedPlayer(removedPlayer);
        transfer.setAddedPlayer(addedPlayer);
        transfer.setRemoved_player_value(removedValue);
        transfer.setAdded_player_value(addedValue);
        transfer.setPenaltyPoints(penaltyPoints);
        transfer.setStatus(TransferStatus.CONFIRMED);
        transfer.setConfirmationDate(LocalDateTime.now());
        transfer.setCreatedBy(createdBy);

        transferDAO.saveTransfer(transfer)
                .orElseThrow(() -> new DataAccessException("Unable to save transfer", null));

        return TransferResponseDTO.builder()
                .transferId(transfer.getTransferId())
                .teamId(team.getTeamId())
                .roundId(currentRound.getRoundId())
                .removed_player_id(removedPlayer.getPlayerId())
                .added_player_id(addedPlayer.getPlayerId())
                .removed_player_name(removedPlayer.getPlayerName())
                .added_player_name(addedPlayer.getPlayerName())
                .removed_player_value(removedValue)
                .added_player_value(addedValue)
                .valueDifference(addedValue.subtract(removedValue))
                .penaltyPoints(penaltyPoints)
                .status(transfer.getStatus().name())
                .transferDate(transfer.getTransferDate())
                .confirmationDate(transfer.getConfirmationDate())
                .build();
    }

    @Override
    public List<TransferResponseDTO> getTransfersForTeam(UUID requestingUserId, UUID teamId) {
        FantasyTeam team = fantasyTeamDAO.getTeamById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found."));

        if (!team.getOwner().getUserId().equals(requestingUserId)) {
            throw new AuthorisationException("You do not own this fantasy team.");
        }

        List<Transfer> transfers = transferDAO.getTransfersByTeamId(teamId);

        return transfers.stream()
                .map(t -> TransferResponseDTO.builder()
                        .transferId(t.getTransferId())
                        .teamId(teamId)
                        .roundId(t.getRound() != null ? t.getRound().getRoundId() : null)
                        .removed_player_id(
                                t.getRemovedPlayer() != null ? t.getRemovedPlayer().getPlayerId() : null)
                        .added_player_id(
                                t.getAddedPlayer() != null ? t.getAddedPlayer().getPlayerId() : null)
                        .removed_player_name(
                                t.getRemovedPlayer() != null ? t.getRemovedPlayer().getPlayerName() : null)
                        .added_player_name(
                                t.getAddedPlayer() != null ? t.getAddedPlayer().getPlayerName() : null)
                        .removed_player_value(t.getRemoved_player_value())
                        .added_player_value(t.getAdded_player_value())
                        .valueDifference(
                                t.getAdded_player_value() != null
                                        && t.getRemoved_player_value() != null ? t.getAdded_player_value().subtract(t.getRemoved_player_value()) : null)
                        .penaltyPoints(t.getPenaltyPoints())
                        .status(t.getStatus() != null ? t.getStatus().name() : null)
                        .transferDate(t.getTransferDate())
                        .confirmationDate(t.getConfirmationDate())
                        .build())
                .collect(Collectors.toList());
    }
}

