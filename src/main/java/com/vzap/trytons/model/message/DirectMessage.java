package com.vzap.trytons.model.message;

import com.vzap.trytons.enums.DirectMessageStatus;
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
public class DirectMessage {
    private UUID messageId;

    private UUID senderUserId;
    private UUID recipientUserId;

    private String body;

    private LocalDateTime createdAt;

    private Boolean isRead;

    private DirectMessageStatus status;
}
