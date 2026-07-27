package com.vzap.trytons.dao.message;

import com.vzap.trytons.enums.MessageRequestStatus;
import com.vzap.trytons.model.message.MessageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRequestDAO {

    MessageRequest create(MessageRequest request);

    Optional<MessageRequest> findById(UUID requestId);

    Optional<MessageRequest> findByPair(UUID requesterUserId, UUID targetUserId);

    List<MessageRequest> findIncoming(UUID targetUserId);

    List<MessageRequest> findOutgoing(UUID requesterUserId);

    boolean updateStatus(UUID requestId, MessageRequestStatus status, LocalDateTime respondedAt);

    /**
     * Re-opens a previously REJECTED request so the same requester/target pair
     * can be re-requested without violating the unique (requester, target) key.
     */
    boolean reopenAsPending(UUID requestId);

    boolean existsApprovedEitherDirection(UUID userA, UUID userB);
}
