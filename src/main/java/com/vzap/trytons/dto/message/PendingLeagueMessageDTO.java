package com.vzap.trytons.dto.message;

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
public class PendingLeagueMessageDTO {
    private UUID messageId;

    private UUID leagueId;
    private String leagueName;

    private UUID senderUserId;
    private String senderUsername;

    private String body;

    private String flaggedReason;

    private LocalDateTime createdAt;
}
