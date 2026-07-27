package com.vzap.trytons.model.message;

import com.vzap.trytons.enums.MessageScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A user's report of a single message. messageId points at either a
 * direct_message or a league_message row depending on messageScope; it
 * cannot be a real foreign key since it targets one of two tables.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MessageReport {
    private UUID reportId;

    private UUID reporterUserId;
    private UUID messageId;
    private MessageScope messageScope;

    private String reason;

    private LocalDateTime createdAt;
}
