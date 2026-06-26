package com.vzap.trytons.service;

import com.vzap.trytons.dao.RegisteredUserDAO;
import com.vzap.trytons.dao.UserDAO;
import com.vzap.trytons.enums.RegistrationStatus;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.RegisteredUser;
import com.vzap.trytons.util.PasswordUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class RegisteredUserServicesImpl implements RegisteredUserServices {

    @Inject
    private UserDAO userDAO;
    @Inject
    private RegisteredUserDAO registeredUserDAO;

    @Override
    public RegisteredUser registeredUser(RegisteredUser newUser) {

        if (userDAO.emailExists(newUser.getEmail())) {
            throw new ConflictException("Email is already in use.");
        }
        if (userDAO.usernameExists(newUser.getUsername())) {
            throw new ConflictException("Username is already being used.");
        }
        newUser.setUserId(UUID.randomUUID());
        newUser.setRegistrationDate(LocalDateTime.now());
        String rawPassword = newUser.getPasswordHash();
        newUser.setPasswordHash(PasswordUtil.hashPassword(rawPassword));
        if (newUser.getRole() == null) {
            newUser.setRole(UserRole.REGISTERED_USER);
        }
        if (newUser.getRegistrationStatus() == null) {
            newUser.setRegistrationStatus(RegistrationStatus.PENDING);
        }
        userDAO.registerUser(newUser).orElseThrow(() -> new DataAccessException("Failed to create user account.", null));
        return registeredUserDAO.register(newUser).orElseThrow(() -> new DataAccessException("Failed to register user.", null));
    }
}
