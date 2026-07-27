package com.vzap.trytons.service.message;

import com.vzap.trytons.dto.message.CreateMessageRequestDTO;
import com.vzap.trytons.dto.message.MessageRequestOverviewDTO;
import com.vzap.trytons.dto.message.MessageRequestResponseDTO;

import java.util.UUID;

public interface MessageRequestService {

    MessageRequestResponseDTO createRequest(UUID actorUserId, CreateMessageRequestDTO request);

    MessageRequestOverviewDTO listRequests(UUID actorUserId);

    MessageRequestResponseDTO approve(UUID actorUserId, UUID requestId);

    MessageRequestResponseDTO reject(UUID actorUserId, UUID requestId);
}
