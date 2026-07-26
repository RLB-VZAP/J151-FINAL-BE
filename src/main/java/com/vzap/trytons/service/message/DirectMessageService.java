package com.vzap.trytons.service.message;

import com.vzap.trytons.dto.message.ConversationThreadDTO;
import com.vzap.trytons.dto.message.DirectMessageResponseDTO;
import com.vzap.trytons.dto.message.SendDirectMessageRequestDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DirectMessageService {

    DirectMessageResponseDTO send(UUID actorUserId, SendDirectMessageRequestDTO request);

    List<ConversationThreadDTO> getThreads(UUID actorUserId);

    List<DirectMessageResponseDTO> getConversation(UUID actorUserId, UUID counterpartUserId, LocalDateTime since);

    int markThreadRead(UUID actorUserId, UUID counterpartUserId);

    int getUnreadCount(UUID actorUserId);
}
