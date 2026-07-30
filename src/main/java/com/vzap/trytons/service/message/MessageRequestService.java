package com.vzap.trytons.service.message;

import com.vzap.trytons.dto.message.CreateMessageRequestRequestDTO;
import com.vzap.trytons.dto.message.MessageContactDTO;
import com.vzap.trytons.dto.message.MessageRequestResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Permission to exchange direct messages. A direct message may only be sent
 * between two users once one has asked and the other accepted; league chat is
 * governed by league membership instead and does not come through here.
 */
public interface MessageRequestService {

    /** Requests awaiting the caller's decision. */
    List<MessageRequestResponseDTO> listIncoming(UUID actorUserId);

    /** Requests the caller has sent that are still pending. */
    List<MessageRequestResponseDTO> listOutgoing(UUID actorUserId);

    MessageRequestResponseDTO create(UUID actorUserId, CreateMessageRequestRequestDTO request);

    MessageRequestResponseDTO accept(UUID actorUserId, UUID requestId);

    MessageRequestResponseDTO decline(UUID actorUserId, UUID requestId);

    /** Users the caller could message, with the state of each relationship. */
    List<MessageContactDTO> listContacts(UUID actorUserId, String searchTerm);

    /** True when direct messages are open between the two users. */
    boolean canExchangeMessages(UUID userA, UUID userB);
}
