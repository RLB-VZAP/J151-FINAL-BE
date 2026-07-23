package com.vzap.trytons.dao.device;

import com.vzap.trytons.model.device.DeviceToken;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DeviceTokenDAO {

    DeviceToken upsert(DeviceToken token);

    List<DeviceToken> findActiveByUserId(UUID userId);

    List<DeviceToken> findActiveByUserIds(Collection<UUID> userIds);

    boolean deactivate(String token);
}
