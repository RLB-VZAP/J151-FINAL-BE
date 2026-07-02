package com.vzap.trytons.dto;

import com.vzap.trytons.enums.RegistrationStatus;
import com.vzap.trytons.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RegisteredUserResponse {
    private final UUID userId;
    private final String username;
    private final UserRole role;
    private final RegistrationStatus status;
}
