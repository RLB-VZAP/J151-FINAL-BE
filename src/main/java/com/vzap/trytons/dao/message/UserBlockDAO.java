package com.vzap.trytons.dao.message;

import com.vzap.trytons.model.message.UserBlock;

import java.util.List;
import java.util.UUID;

public interface UserBlockDAO {

    UserBlock create(UserBlock block);

    boolean delete(UUID blockerUserId, UUID blockedUserId);

    boolean existsEitherDirection(UUID userA, UUID userB);

    List<UserBlock> findByBlocker(UUID blockerUserId);
}
