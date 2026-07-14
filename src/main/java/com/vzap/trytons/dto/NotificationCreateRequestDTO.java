package com.vzap.trytons.dto;

import com.vzap.trytons.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationCreateRequestDTO {
    private UUID userId;
    private NotificationType type;
    private String body;
    private String relatedEntityType;
    private UUID relatedEntityId;
}
