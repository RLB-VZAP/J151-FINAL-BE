package com.vzap.trytons.model.device;

import com.vzap.trytons.enums.DevicePlatform;
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
public class DeviceToken {
    private UUID tokenId;

    private UUID userId;

    private String token;

    private DevicePlatform platform;

    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime lastSeenAt;
}
