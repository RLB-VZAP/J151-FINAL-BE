package com.vzap.trytons.model.message;

import com.vzap.trytons.enums.MessageRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A private-chat consent request from one user to another. Rule A: a direct
 * message may not be sent until the target has approved a request like this.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MessageRequest {
    private UUID requestId;

    private UUID requesterUserId;
    private UUID targetUserId;

    private MessageRequestStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
