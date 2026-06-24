package com.vzap.trytons.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

public class PrivateMessage {
    private UUID privateMessageId;
    private String content;
    private LocalDateTime sentDate;
    private Boolean removed;
    private Boolean isRead;
}
