package com.vzap.trytons.model.message;

import com.vzap.trytons.enums.MessageRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MessageRequest {
    private UUID requestId;

    private UUID requesterUserId;
    private UUID addresseeUserId;

    private MessageRequestStatus status;
    private String introMessage;

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    /** Denormalised for display; not stored on message_request. */
    private String requesterUsername;
    private String addresseeUsername;
}
