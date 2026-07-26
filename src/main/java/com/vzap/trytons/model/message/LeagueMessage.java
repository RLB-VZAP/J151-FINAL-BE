package com.vzap.trytons.model.message;

import com.vzap.trytons.enums.MessageStatus;
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
public class LeagueMessage {
    private UUID messageId;

    private UUID leagueId;
    private UUID senderUserId;

    private String body;

    private MessageStatus status;

    private String flaggedReason;

    private UUID moderatedByUserId;
    private LocalDateTime moderatedAt;

    private LocalDateTime createdAt;
}
