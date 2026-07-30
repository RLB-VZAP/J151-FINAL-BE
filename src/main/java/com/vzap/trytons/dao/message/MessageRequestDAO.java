package com.vzap.trytons.dao.message;

import com.vzap.trytons.model.message.MessageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRequestDAO {

    MessageRequest create(MessageRequest request);

    Optional<MessageRequest> findById(UUID requestId);

    /** The request this exact requester sent this exact addressee, if any. */
    Optional<MessageRequest> findByPair(UUID requesterUserId, UUID addresseeUserId);

    /**
     * Any request between the two users, whichever of them sent it. Used to
     * decide whether a direct message is allowed, since acceptance is mutual.
     */
    Optional<MessageRequest> findBetween(UUID userA, UUID userB);

    /** True when a request between the two has been accepted, either direction. */
    boolean isAcceptedBetween(UUID userA, UUID userB);

    /** Requests awaiting this user's decision, newest first. */
    List<MessageRequest> findPendingForAddressee(UUID addresseeUserId);

    /** Requests this user has sent that are still waiting, newest first. */
    List<MessageRequest> findPendingFromRequester(UUID requesterUserId);

    /** All requests involving this user, either direction. */
    List<MessageRequest> findAllInvolving(UUID userId);

    /** Applies a decision (or re-opens a declined request as PENDING). */
    boolean updateStatus(UUID requestId, com.vzap.trytons.enums.MessageRequestStatus status);

    /** Re-opens an existing row instead of inserting a duplicate pair. */
    boolean reopen(UUID requestId, String introMessage);
}
