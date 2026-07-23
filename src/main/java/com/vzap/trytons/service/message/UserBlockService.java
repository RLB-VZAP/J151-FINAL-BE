package com.vzap.trytons.service.message;

import com.vzap.trytons.dto.message.BlockedUserDTO;

import java.util.List;
import java.util.UUID;

public interface UserBlockService {

    void block(UUID actorUserId, UUID targetUserId);

    void unblock(UUID actorUserId, UUID targetUserId);

    List<BlockedUserDTO> listBlocked(UUID actorUserId);
}
