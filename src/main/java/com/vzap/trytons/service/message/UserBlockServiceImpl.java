package com.vzap.trytons.service.message;

import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dao.message.UserBlockDAO;
import com.vzap.trytons.dto.message.BlockedUserDTO;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.model.message.UserBlock;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserBlockServiceImpl implements UserBlockService {

    @Inject
    private UserBlockDAO userBlockDAO;
    @Inject
    private UserDAO userDAO;

    @Override
    public void block(UUID actorUserId, UUID targetUserId) {
        requireAuthenticated(actorUserId);
        if (targetUserId == null) {
            throw new ValidationException("A user to block is required.");
        }
        if (actorUserId.equals(targetUserId)) {
            throw new ValidationException("You cannot block yourself.");
        }

        userDAO.getUserById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User to block not found."));

        boolean alreadyBlocked = userBlockDAO.findByBlocker(actorUserId).stream()
                .anyMatch(block -> targetUserId.equals(block.getBlockedUserId()));
        if (alreadyBlocked) {
            throw new ConflictException("You have already blocked this user.");
        }

        userBlockDAO.create(UserBlock.builder()
                .blockerUserId(actorUserId)
                .blockedUserId(targetUserId)
                .build());
    }

    @Override
    public void unblock(UUID actorUserId, UUID targetUserId) {
        requireAuthenticated(actorUserId);
        if (targetUserId == null) {
            throw new ValidationException("A user to unblock is required.");
        }
        userBlockDAO.delete(actorUserId, targetUserId);
    }

    @Override
    public List<BlockedUserDTO> listBlocked(UUID actorUserId) {
        requireAuthenticated(actorUserId);

        List<BlockedUserDTO> blocked = new ArrayList<>();
        for (UserBlock block : userBlockDAO.findByBlocker(actorUserId)) {
            Optional<User> user = userDAO.getUserById(block.getBlockedUserId());
            blocked.add(BlockedUserDTO.builder()
                    .userId(block.getBlockedUserId())
                    .username(user.map(User::getUsername).orElse("Unknown user"))
                    .blockedAt(block.getCreatedAt())
                    .build());
        }
        return blocked;
    }

    private void requireAuthenticated(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated user is required.");
        }
        User user = userDAO.getUserById(actorUserId)
                .orElseThrow(() -> new AuthorisationException("An authenticated user is required."));
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthorisationException("An authenticated user is required.");
        }
    }
}
