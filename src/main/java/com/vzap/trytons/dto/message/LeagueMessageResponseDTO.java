package com.vzap.trytons.dto.message;

import com.vzap.trytons.enums.MessageStatus;
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
public class LeagueMessageResponseDTO {
    private UUID messageId;

    private UUID leagueId;

    private UUID senderUserId;
    private String senderUsername;

    private String body;

    private MessageStatus status;

    private LocalDateTime createdAt;
}
