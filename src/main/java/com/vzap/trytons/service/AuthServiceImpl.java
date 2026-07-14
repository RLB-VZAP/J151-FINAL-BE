package com.vzap.trytons.service;

import com.vzap.trytons.dao.UserDAO;
import com.vzap.trytons.dto.AuthStatusResponseDTO;
import com.vzap.trytons.dto.LoginResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.User;
import com.vzap.trytons.util.AuthTokenUtil;
import com.vzap.trytons.util.PasswordUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AuthServiceImpl implements AuthService {

    @Inject
    private UserDAO userDAO;

    @Override
    public LoginResponseDTO authenticate(
            String identifier,
            String password) {

        validateCredentials(identifier, password);

        String cleanedIdentifier = identifier.trim();

        Optional<User> possibleUser =
                userDAO.getUserByEmail(cleanedIdentifier);

        if (possibleUser.isEmpty()) {
            possibleUser =
                    userDAO.getUserByUsername(cleanedIdentifier);
        }

        User user = possibleUser.orElseThrow(() ->
                new AuthenticationException(
                        "Invalid email/username or password."
                )
        );

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthorisationException(
                    "This account is inactive."
            );
        }

        if (!PasswordUtil.verifyPassword(
                password,
                user.getPasswordHash())) {

            throw new AuthenticationException(
                    "Invalid email/username or password."
            );
        }

        boolean lastLoginUpdated =
                userDAO.updateLastLogin(
                        user.getUserId(),
                        LocalDateTime.now()
                );

        if (!lastLoginUpdated) {
            throw new DataAccessException(
                    "Unable to update the user's last login time.",
                    null
            );
        }

        String tokenCreated =AuthTokenUtil.createToken(user.getUserId());

        return new LoginResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                tokenCreated);
    }

    @Override
    public String logout() {
        // Authentication is stateless.
        // The frontend removes its session and stored token.
        return "Logout acknowledged.";
    }

    @Override
    public AuthStatusResponseDTO getAuthStatus(
            String requestingUserId) {

        if (requestingUserId == null
                || requestingUserId.isBlank()) {

            return new AuthStatusResponseDTO(
                    false,
                    null,
                    null,
                    null,
                    null
            );
        }

        final UUID userId;

        try {
            userId = UUID.fromString(
                    requestingUserId.trim()
            );
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                    "requestingUserId must be a valid UUID."
            );
        }

        Optional<User> possibleUser =
                userDAO.getUserById(userId);

        if (possibleUser.isEmpty()
                || !Boolean.TRUE.equals(
                possibleUser.get().getIsActive()
        )) {

            return new AuthStatusResponseDTO(
                    false,
                    null,
                    null,
                    null,
                    null
            );
        }

        User user = possibleUser.get();

        return new AuthStatusResponseDTO(
                true,
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    private void validateCredentials(
            String identifier,
            String password) {

        if (identifier == null
                || identifier.isBlank()
                || password == null
                || password.isBlank()) {

            throw new ValidationException(
                    "Email/username and password are required."
            );
        }
    }
}