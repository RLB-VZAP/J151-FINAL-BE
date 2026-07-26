package com.vzap.trytons.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class DirectMessageResponseDTO {
    private UUID messageId;

    private UUID senderUserId;
    private UUID recipientUserId;

    private String body;

    private LocalDateTime createdAt;

    @JsonProperty("isRead")
    private boolean isRead;

    @JsonProperty("mine")
    private boolean mine;
}
