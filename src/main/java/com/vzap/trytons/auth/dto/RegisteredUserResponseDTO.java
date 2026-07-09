package com.vzap.trytons.auth.dto;

import com.vzap.trytons.auth.enums.RegistrationStatus;
import com.vzap.trytons.auth.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RegisteredUserResponseDTO {
    private final UUID userId;
    private final String username;
    private final UserRole role;
    private final RegistrationStatus status;
}
