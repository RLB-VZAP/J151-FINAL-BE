package com.vzap.trytons.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivateMessage {
    private UUID messageId;
    private String content;
    private LocalDateTime sentDate;
    private Boolean removed;
    private Boolean isRead;

    private UUID senderUserId;
    private UUID receiverUserId;
}
