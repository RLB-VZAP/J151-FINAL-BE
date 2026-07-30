package com.vzap.trytons.service.device;

import com.vzap.trytons.dao.device.DeviceTokenDAO;
import com.vzap.trytons.dto.device.RegisterDeviceRequestDTO;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.device.DeviceToken;
import jakarta.inject.Inject;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeviceTokenServiceImpl implements DeviceTokenService {

    @Inject
    private DeviceTokenDAO deviceTokenDAO;

    @Override
    public void register(UUID actorUserId, RegisterDeviceRequestDTO request) {
        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated user is required.");
        }
        if (request == null || request.getToken() == null || request.getToken().isBlank()) {
            throw new ValidationException("A device token is required.");
        }
        if (request.getPlatform() == null) {
            throw new ValidationException("A device platform is required.");
        }

        DeviceToken token = DeviceToken.builder()
                .userId(actorUserId)
                .token(request.getToken().trim())
                .platform(request.getPlatform())
                .isActive(true)
                .build();

        deviceTokenDAO.upsert(token);
    }

    @Override
    public List<String> activeTokensForUser(UUID userId) {
        return deviceTokenDAO.findActiveByUserId(userId).stream()
                .map(DeviceToken::getToken)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> activeTokensForUsers(Collection<UUID> userIds) {
        return deviceTokenDAO.findActiveByUserIds(userIds).stream()
                .map(DeviceToken::getToken)
                .collect(Collectors.toList());
    }
}
