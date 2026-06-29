package com.vzap.trytons.service;

import com.vzap.trytons.dao.TempFantasyTeamDAO;
import com.vzap.trytons.dao.TempPlayerDAO;
import com.vzap.trytons.dao.TransferDAO;
import com.vzap.trytons.dao.TransferHistoryDAO;
import com.vzap.trytons.dto.TransferRequest;
import com.vzap.trytons.dto.TransferResponse;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Player;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

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
    private TempSquadValidationService squadValidationService;

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
            squadValidationService.validatePlayerIsAvailable(addedPlayer);
        }


        return null;
    }

    @Override
    public List<TransferResponse> getTransferHistoryForTeam(UUID requestingUserId, UUID teamId) {
        return List.of();
    }
}
