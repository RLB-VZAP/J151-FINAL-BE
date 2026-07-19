package com.vzap.trytons.service.fixture;

import com.vzap.trytons.dao.admin.AdminDAO;
import com.vzap.trytons.dao.fixture.FantasyRoundDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamRoundSelectionDAO;
import com.vzap.trytons.dao.catalog.PlayerDAO;
import com.vzap.trytons.dao.fixture.RoundLockDAO;
import com.vzap.trytons.dto.fixture.DeadlineStatusResponseDTO;
import com.vzap.trytons.dto.fixture.LockStatusResponseDTO;
import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.enums.RoundLockAction;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.fixture.FantasyRound;
import com.vzap.trytons.model.fantasyteam.FantasyTeamRoundSelection;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.fixture.RoundLock;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class DeadlineLockServiceImpl implements DeadlineLockService {
    @Inject
    private FantasyTeamRoundSelectionDAO fantasyTeamRoundSelectionDAO;
    @Inject
    private RoundLockDAO roundLockDAO;
    @Inject
    private FantasyRoundDAO fantasyRoundDAO;
    @Inject
    private PlayerDAO playerDAO;
    @Inject
    private AdminDAO adminDAO;


    @Override
    public LockStatusResponseDTO getLockStatus(UUID roundId) {
        FantasyRound round = fantasyRoundDAO.getRoundById(roundId).orElseThrow(() -> new ResourceNotFoundException("Fantasy round not found."));
        LockStatusResponseDTO response = new LockStatusResponseDTO();
        response.setRoundId(roundId);
        boolean locked = false;
        Optional<RoundLock> latestLock = roundLockDAO.getLatestRoundLockByRoundId(roundId);
        if (latestLock.isPresent()) {
            if(latestLock.get().getLockAction() == RoundLockAction.LOCKED){
                locked = true;
            }
        }else{
            if(round.getStatus() == FantasyRoundStatus.LOCKED || round.getStatus() == FantasyRoundStatus.IN_PROGRESS || round.getStatus() == FantasyRoundStatus.COMPLETED){
                locked = true;
            }
        }
        response.setLocked(locked);
        boolean snapshotCreated = fantasyTeamRoundSelectionDAO.snapshotsExistForRound(roundId);
        response.setSnapshotsCreated(snapshotCreated);
        response.setLockedTeamIds(getLockedTeamIds(roundId));
        List<UUID> lockedPlayerIds = new ArrayList<>();
        for(UUID teamId : response.getLockedTeamIds()){
            lockedPlayerIds.addAll(getLockedPlayerIds(roundId, teamId));
        }
        response.setLockedPlayerIds(lockedPlayerIds);
        return response;
    }

    @Override
    public DeadlineStatusResponseDTO getDeadlineStatus(UUID roundId) {
        FantasyRound round = fantasyRoundDAO.getRoundById(roundId).orElseThrow(() -> new ResourceNotFoundException("Fantasy round not found"));
        DeadlineStatusResponseDTO response = new DeadlineStatusResponseDTO();
        response.setRoundId(round.getRoundId());
        response.setRoundStatus(round.getStatus());
        response.setOpenDate(round.getOpenDate());
        response.setLockDeadline(round.getLockDeadline());
        response.setEndDate(round.getEndDate());
        boolean locked = round.getStatus() == FantasyRoundStatus.IN_PROGRESS || round.getStatus() == FantasyRoundStatus.COMPLETED;
        response.setLocked(locked);
        boolean openForTransfer = round.getStatus() == FantasyRoundStatus.OPEN && LocalDateTime.now().isBefore(round.getLockDeadline());
        response.setOpenForTransfers(openForTransfer);
        if(openForTransfer) {
            response.setMessage("Round is open for transfers.");
        } else if (locked) {
            response.setMessage("Round is locked for transfers.");
        }else{
            response.setMessage("Transfers are unavailable for this round.");
        }
        return response;
    }

    @Override
    public LockStatusResponseDTO lockRound(UUID actorAdminUserId, UUID roundId, String reason) {
        fantasyRoundDAO.getRoundById(roundId).orElseThrow(() -> new ResourceNotFoundException("Fantasy round not found."));
        RoundLock roundLock = new RoundLock();
        roundLock.setRoundId(roundId);
        if(adminDAO.getAdminById(actorAdminUserId).isPresent()) {
            roundLock.setActionByAdminUserId(actorAdminUserId);
        }else{
            throw new AuthorisationException("Only administrators can lock rounds.");
        }
        roundLock.setReason(reason);
        roundLock.setLockAction(RoundLockAction.LOCKED);
        roundLock.setActionAt(LocalDateTime.now());
        roundLockDAO.createRoundLock(roundLock);
        fantasyRoundDAO.updateRoundStatus(roundId,FantasyRoundStatus.LOCKED);
        return getLockStatus(roundId);
    }

    @Override
    public List<UUID> getLockedTeamIds(UUID roundId) {
        fantasyRoundDAO.getRoundById(roundId).orElseThrow(() -> new ResourceNotFoundException("Fantasy round not found"));
        return fantasyTeamRoundSelectionDAO.getTeamIdsWithSnapshotsForRound(roundId);
    }

    @Override
    public List<UUID> getLockedPlayerIds(UUID roundId, UUID teamId) {
        fantasyRoundDAO.getRoundById(roundId).orElseThrow(() -> new ResourceNotFoundException("Fantasy round not found"));
        List<FantasyTeamRoundSelection> selections = fantasyTeamRoundSelectionDAO.getSelectionsByRoundIdAndTeamId(roundId, teamId);
        List<UUID> lockedPlayerIds = new ArrayList<>();
        for(FantasyTeamRoundSelection selection : selections){
            lockedPlayerIds.add(selection.getPlayerId());
        }
        return lockedPlayerIds;
    }

    @Override
    public List<Player> getAvailableTransferPlayers(UUID roundId, UUID teamId) {
        assertTransferAllowed(roundId, teamId);
        List<Player> players = playerDAO.getAllPlayers();
        List<UUID> lockedPlayers = getLockedPlayerIds(roundId,teamId);
        List<Player> availablePlayers = new ArrayList<>();
        for(Player player : players){
            if(!lockedPlayers.contains(player.getPlayerId())) {
                availablePlayers.add(player);
            }
        }
        return availablePlayers;
    }

    @Override
    public void assertTransferAllowed(UUID roundId, UUID teamId) {
        DeadlineStatusResponseDTO response = getDeadlineStatus(roundId);
        if(response.isLocked()){
            throw new BusinessRuleException("Transfers are not allowed because this round is locked.");
        }
        if(!response.isOpenForTransfers()){
            throw new BusinessRuleException("Transfers are not allowed because the deadline has passed.");
        }

    }
}