package com.vzap.trytons.service;

import com.vzap.trytons.dao.TempFantasyTeamDAO;
import com.vzap.trytons.dao.TempPlayerDAO;
import com.vzap.trytons.dao.TransferDAO;
import com.vzap.trytons.dao.TransferHistoryDAO;
import com.vzap.trytons.dto.SquadValidationResult;
import com.vzap.trytons.dto.TransferRequest;
import com.vzap.trytons.dto.TransferResponse;
import com.vzap.trytons.enums.TransferWindowStatus;
import com.vzap.trytons.exceptions.*;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Player;
import com.vzap.trytons.model.Transfer;
import com.vzap.trytons.model.TransferHistory;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TransferServiceImpl implements TransferService {

    private static final int FREE_TRANSFERS_PER_ROUND = 1;
    private static final int PENALTY_POINTS_PER_EXTRA_TRANSFER = 4;

    @Inject
    private TransferDAO transferDAO;
    @Inject
    private TransferHistoryDAO transferHistoryDAO;
    @Inject
    private TempFantasyTeamDAO fantasyTeamDAO;
    @Inject
    private TempPlayerDAO playerDAO;
    @Inject
    private SquadValidationService squadValidationService;

    @Override
    public TransferResponse executeTransfer(UUID requestingUserId, TransferRequest request) {
        if(request.getRemovedPlayerId() == null && request.getAddedPlayerId() == null) {
            throw new ValidationException("A transfer must include a player to remove and/or add");
        }

        FantasyTeam team = fantasyTeamDAO.getTeamById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found"));

        if (!team.getOwner().getUserId().equals(requestingUserId)) {
            throw new AuthorisationException("You do not own this fantasy team");
        }

        if (Boolean.TRUE.equals(team.getIsLocked())){
            throw new BusinessRuleException("This team cannot be transferred while locked");
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

        SquadValidationResult validationResult = squadValidationService.validateTransfer(
                request.getTeamId(),
                request.getRemovedPlayerId(),
                request.getAddedPlayerId()
        );
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getErrors().get(0).getMessage());
        }

        BigDecimal removedValue = removedPlayer != null ? removedPlayer.getValue() : BigDecimal.ZERO;
        BigDecimal addedValue = addedPlayer != null ? addedPlayer.getValue() : BigDecimal.ZERO;
        BigDecimal oldRemainingBudget = team.getRemainingBudget();
        BigDecimal oldTeamValue = team.getTotalTeamValue();
        BigDecimal newRemainingBudget = oldRemainingBudget.add(removedValue).subtract(addedValue);
        BigDecimal newTeamValue = oldTeamValue.subtract(removedValue).add(addedValue);

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

        return new TransferResponse(
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
    public List<TransferResponse> getTransferHistoryForTeam(UUID requestingUserId, UUID teamId) {
        FantasyTeam team = fantasyTeamDAO.getTeamById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found."));

        if(!team.getOwner().equals(requestingUserId)){
            throw new AuthorisationException("You do not own this fantasy team.");
        }

        List<Transfer> transfers = transferDAO.getTransfersByTeamId(teamId);
        return transfers.stream()
                .map(t -> new TransferResponse(
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
