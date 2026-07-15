package com.vzap.trytons.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

public class ChatMessage {
    private UUID messageId;
    private String content;
    private LocalDateTime sentDate;
    private Boolean removed;

    private UUID leagueId;

    private UUID senderUserId;
    private UUID removedByUserId;
}
