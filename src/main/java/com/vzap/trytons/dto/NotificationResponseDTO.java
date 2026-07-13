package com.vzap.trytons.dto;

import com.vzap.trytons.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
public class NotificationResponseDTO {
    private UUID notificationId;
    private NotificationType type;
    private String body;
    private LocalDateTime createdAt;
    private boolean isRead;
    private String relatedEntityType;
    private UUID relatedEntityId;
}
