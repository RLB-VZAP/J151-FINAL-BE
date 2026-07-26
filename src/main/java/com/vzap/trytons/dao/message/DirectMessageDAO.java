package com.vzap.trytons.dao.message;

import com.vzap.trytons.model.message.ConversationThread;
import com.vzap.trytons.model.message.DirectMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DirectMessageDAO {

    DirectMessage create(DirectMessage message);

    List<DirectMessage> findConversation(UUID userA, UUID userB, LocalDateTime since);

    List<ConversationThread> findThreads(UUID userId);

    int markThreadRead(UUID recipientUserId, UUID counterpartUserId);

    int countUnread(UUID userId);
}
