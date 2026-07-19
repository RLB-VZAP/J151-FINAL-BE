package com.vzap.trytons.service.auth;

import com.vzap.trytons.dao.auth.RegisteredUserDAO;
import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.auth.RegisteredUserRequestDTO;
import com.vzap.trytons.enums.RegistrationStatus;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.auth.RegisteredUser;
import com.vzap.trytons.util.PasswordUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class RegisteredUserServicesImpl implements RegisteredUserServices {

    @Inject
    private UserDAO userDAO;
    @Inject
    private RegisteredUserDAO registeredUserDAO;

    @Override
    public RegisteredUser registerUser(RegisteredUserRequestDTO userRequest) {
        String email = userRequest.getEmail();
        String username = userRequest.getUsername();
        String rawPassword = userRequest.getRawPassword();

        if (userDAO.emailExists(email)){
            throw new ConflictException("Email is already in use.");
        }
        if (userDAO.usernameExists(username)) {
            throw new ConflictException("Username is already being used.");
        }
        RegisteredUser newUser= new RegisteredUser();

        newUser.setUserId(UUID.randomUUID());
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setRegistrationDate(LocalDateTime.now());
        newUser.setPasswordHash(PasswordUtil.hashPassword(rawPassword));
        newUser.setRole(UserRole.REGISTERED_USER);
        newUser.setRegistrationStatus(RegistrationStatus.PENDING);
        newUser.setIsActive(true);
        return registeredUserDAO.register(newUser).orElseThrow(() -> new DataAccessException("Failed to register user.", null));
    }
}
