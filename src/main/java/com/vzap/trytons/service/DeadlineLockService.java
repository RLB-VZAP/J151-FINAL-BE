package com.vzap.trytons.service;

import com.vzap.trytons.dto.DeadlineStatusResponseDTO;
import com.vzap.trytons.dto.LockStatusResponseDTO;
import com.vzap.trytons.model.Player;

import java.util.List;
import java.util.UUID;

public class DeadlineLockService {
    //STUB
    public LockStatusResponseDTO getLockStatus(UUID fixtureId){
        //This is a STUB please complete.
        throw  new UnsupportedOperationException("STUB");
    }
    public DeadlineStatusResponseDTO getDeadlineStatus(UUID fixtureId){
        //This is a STUB please complete.
        throw  new UnsupportedOperationException("STUB");
    }
    public List<UUID> getLockedTeamIds(UUID fixtureId) {
        //STUB please complete
        throw  new UnsupportedOperationException("STUB");
    }
    public List<UUID> getLockedPlayerIds(UUID fixtureId) {
        //STUB please complete
        throw  new UnsupportedOperationException("STUB");
    }
    public List<Player> getAvailableTransferPlayers(UUID fixtureId) {
        //STUB please complete
        throw  new UnsupportedOperationException("STUB");
    }

}
