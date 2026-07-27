package com.vzap.trytons.dao.message;

import com.vzap.trytons.model.message.ConversationThread;
import com.vzap.trytons.model.message.DirectMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DirectMessageDAO {

    DirectMessage create(DirectMessage message);

    Optional<DirectMessage> findById(UUID messageId);

    List<DirectMessage> findConversation(UUID userA, UUID userB, LocalDateTime since);

    List<ConversationThread> findThreads(UUID userId);

    int markThreadRead(UUID recipientUserId, UUID counterpartUserId);

    int countUnread(UUID userId);

    /**
     * Rule C grandfathering: true if the pair already has at least one
     * delivered (APPROVED) message, regardless of who sent it.
     */
    boolean existsApprovedMessageBetween(UUID userA, UUID userB);

    /**
     * Admin-only bounded read (rule F): the message at anchorMessageId plus up
     * to windowSize immediately preceding messages between the same pair,
     * ordered oldest to newest. Includes all statuses so evasion attempts are
     * visible to the reviewer.
     */
    List<DirectMessage> findAdminWindow(UUID userA, UUID userB, LocalDateTime anchorCreatedAt,
                                        UUID anchorMessageId, int windowSize);
}
