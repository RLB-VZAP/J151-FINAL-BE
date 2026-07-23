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
public class ConversationThreadDTO {
    private UUID counterpartUserId;
    private String counterpartUsername;

    private String lastMessageBody;
    private LocalDateTime lastMessageAt;

    private int unreadCount;
}
