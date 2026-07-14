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

@ApplicationScoped
public class AuthServiceImpl implements AuthService {

    @Inject
    private UserDAO userDAO;

    @Override
    public LoginResponseDTO authenticate(String identifier, String password) {

        validateCredentials(identifier, password);
        String cleanedIdentifier = identifier.trim();
        Optional<User> possibleUser = userDAO.getUserByEmail(cleanedIdentifier);

        if (possibleUser.isEmpty()) {
            possibleUser = userDAO.getUserByUsername(cleanedIdentifier);
        }

        User user = possibleUser.orElseThrow(() ->
                new AuthenticationException("Invalid email/username or password."));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthorisationException("This account is inactive.");
        }

        boolean passwordMatches = PasswordUtil.verifyPassword(password, user.getPasswordHash());
        if (!passwordMatches) {
            throw new AuthenticationException("Invalid email/username or password.");
        }

        boolean lastLoginUpdated = userDAO.updateLastLogin(user.getUserId(), LocalDateTime.now());
        if (!lastLoginUpdated) {
            throw new DataAccessException("Unable to update the user's last login time.", null);
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
        return "";
    }

    @Override
    public AuthStatusResponseDTO getAuthStatus(String requestingUserId) {
        return null;
    }

    private void validateCredentials(String identifier, String password) {
        if (identifier == null || identifier.isBlank() || password == null || password.isBlank()) {
            throw new ValidationException("Email/username and password are required.");
        }
    }
}
