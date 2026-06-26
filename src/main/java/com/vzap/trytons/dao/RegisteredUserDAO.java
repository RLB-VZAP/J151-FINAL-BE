package com.vzap.trytons.dao;

import com.vzap.trytons.model.RegisteredUser;

import java.lang.ScopedValue;
import java.util.Optional;
import java.util.UUID;

public interface RegisteredUserDAO {
    Optional<RegisteredUser> getRegisteredUserById(UUID userId);
    Optional<RegisteredUser> updateProfile(RegisteredUser registeredUser);
    boolean deactivateAccount(UUID userId);

   Optional<RegisteredUser> register(RegisteredUser newUser);
}
