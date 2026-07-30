package com.vzap.trytons.dto.message;

import com.vzap.trytons.enums.MessageRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequestResponseDTO {
    private UUID requestId;

    private UUID requesterUserId;
    private String requesterUsername;
    private UUID addresseeUserId;
    private String addresseeUsername;

    private MessageRequestStatus status;
    private String introMessage;

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    /** True when the caller is the one who sent the request. */
    private boolean mine;
}
