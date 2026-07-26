package com.vzap.trytons.model.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-only projection describing one 1:1 conversation from a given user's
 * point of view: the other participant, the most recent message, and how many
 * messages from them the user has not yet read.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConversationThread {
    private UUID counterpartUserId;
    private String counterpartUsername;

    private String lastMessageBody;
    private LocalDateTime lastMessageAt;

    private int unreadCount;
}
