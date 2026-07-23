package com.vzap.trytons.service.device;

import com.vzap.trytons.dto.device.RegisterDeviceRequestDTO;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DeviceTokenService {

    void register(UUID actorUserId, RegisterDeviceRequestDTO request);

    List<String> activeTokensForUser(UUID userId);

    List<String> activeTokensForUsers(Collection<UUID> userIds);
}
