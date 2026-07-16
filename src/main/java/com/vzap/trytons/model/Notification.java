package com.vzap.trytons.model;

import com.vzap.trytons.enums.NotificationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Notification {
    private UUID notificationId;
    private NotificationType type;
    private String body;
    private LocalDateTime createdAt;
    private Boolean isRead;
    private String relatedEntityType;
    private UUID relatedEntityId;
    private UUID userId;
}