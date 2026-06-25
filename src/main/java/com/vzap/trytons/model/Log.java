package com.vzap.trytons.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

public class Log {
    private UUID logId;
    private String entityType;
    private UUID entityId;
    private String actionType;
    private String description;
    private LocalDateTime createdAt;
    private String ipAddress;

    private Notification notification;

    private User user;
    private Transfer transfer;
}
