package com.vzap.trytons.model.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserBlock {
    private UUID blockId;

    private UUID blockerUserId;
    private UUID blockedUserId;

    private LocalDateTime createdAt;
}
