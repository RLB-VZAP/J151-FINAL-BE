package com.vzap.trytons.service;

import com.vzap.trytons.dao.*;
import com.vzap.trytons.dto.TransferRequestDTO;
import com.vzap.trytons.dto.TransferResponseDTO;
import com.vzap.trytons.enums.TransferWindowStatus;
import com.vzap.trytons.exceptions.*;
import com.vzap.trytons.model.*;
import jakarta.inject.Inject;

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
    private TransferHistoryDAO transferHistoryDAO;
    @Inject
    private FantasyTeamDAO fantasyTeamDAO;
    @Inject
    private FantasyTeamPlayerDAO fantasyTeamPlayerDAO;
    @Inject
    private PlayerDAO playerDAO;
    @Inject
    private SquadValidationService squadValidationService;

    @Override
    public TransferResponseDTO executeTransfer(UUID requestingUserId, TransferRequestDTO request) {
        if(request.getRemovedPlayerId() == null && request.getAddedPlayerId() == null) {
            throw new ValidationException("A transfer must include a player to remove and/or add");
        }

        if (request.getRemovedPlayerId() != null
                && request.getAddedPlayerId() != null
                && request.getRemovedPlayerId().equals(request.getAddedPlayerId())) {
            throw new ValidationException("The same player cannot be both added and removed in one transfer.");

        FantasyTeam team = fantasyTeamDAO.getTeamById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found"));

        if (!team.getOwner().getUserId().equals(requestingUserId)) {
            throw new AuthorisationException("You do not own this fantasy team");
        }

        if (Boolean.TRUE.equals(team.getIsLocked())){
            throw new BusinessRuleException("This team cannot be transferred while locked");
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
        if(request.getAddedPlayerId() != null) {
            addedPlayer = playerDAO.getPlayerById(request.getAddedPlayerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Player not found"));
        }

        Player removedPlayer = null;
        if(request.getRemovedPlayerId() != null) {
            removedPlayer = playerDAO.getPlayerById(request.getRemovedPlayerId())
                    .orElseThrow(() ->  new ResourceNotFoundException("Player not found"));
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



        int transfersAlreadyThisRound = transferDAO.getTransfersByTeamId(team.getTeamId()).size();
        boolean penaltyApplied = transfersAlreadyThisRound >= FREE_TRANSFERS_PER_ROUND;
        int penaltyPoints = penaltyApplied ? PENALTY_POINTS_PER_EXTRA_TRANSFER : 0;

        Transfer transfer = new Transfer();
        transfer.setTransferId(UUID.randomUUID());
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setPenaltyApplied(penaltyApplied);
        transfer.setPenaltyPoints(penaltyPoints);
        transfer.setTransferWindowStatus(TransferWindowStatus.OPEN);
        transfer.setConfirmed(true);
        transfer.setFantasyTeam(team);
        transfer.setAddedPlayer(addedPlayer);
        transfer.setRemovedPlayer(removedPlayer);

        transferDAO.saveTransfer(transfer)
                .orElseThrow(() -> new DataAccessException("Unable to save transfer", null));

        boolean budgetUpdated = fantasyTeamDAO.updateBudgetAndValue(team.getTeamId(), newRemainingBudget, newTeamValue);
        if(!budgetUpdated){
            throw new DataAccessException("Unable to update budget after transfer", null);
        }

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

        return new TransferResponseDTO(
                transfer.getTransferId(),
                team.getTeamId(),
                removedPlayer != null ? removedPlayer.getPlayerId() : null,
                addedPlayer != null ? addedPlayer.getPlayerId() : null,
                transfer.getTransferDate(),
                penaltyApplied,
                penaltyPoints,
                newRemainingBudget,
                newTeamValue
        );
    }

    @Override
    public List<TransferResponseDTO> getTransferHistoryForTeam(UUID requestingUserId, UUID teamId) {
        FantasyTeam team = fantasyTeamDAO.getTeamById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found."));

        if(!team.getOwner().equals(requestingUserId)){
            throw new AuthorisationException("You do not own this fantasy team.");
        }

        List<Transfer> transfers = transferDAO.getTransfersByTeamId(teamId);
        return transfers.stream()
                .map(t -> new TransferResponseDTO(
                t.getTransferId(),
                teamId,
                t.getRemovedPlayer() != null ? t.getRemovedPlayer().getPlayerId() : null,
                t.getAddedPlayer() != null ? t.getAddedPlayer().getPlayerId() : null,
                t.getTransferDate(),
                Boolean.TRUE.equals(t.getPenaltyApplied()),
                t.getPenaltyPoints(),
                null,
                null
        )).collect(Collectors.toList());
    }
}
