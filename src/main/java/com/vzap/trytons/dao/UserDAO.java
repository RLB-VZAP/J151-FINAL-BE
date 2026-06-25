package com.vzap.trytons.dao;

import com.vzap.trytons.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserDAO {
    Optional<User> getUserById(UUID userId);
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByUsername(String username);
    Optional<User> registerUser(User user);
    Optional<User> updateUser(User user);
    boolean emailExists(String email);
    boolean usernameExists(String username);
}
