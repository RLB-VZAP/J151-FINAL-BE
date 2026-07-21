package com.vzap.trytons.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vzap.trytons.enums.RegistrationStatus;
import com.vzap.trytons.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class ProfileResponseDTO {
    private final UUID userId;
    private final String email;
    private final String username;
    private final UserRole role;
    @JsonProperty("isActive")
    private final Boolean isActive;
    private final String profilePic;
    private final LocalDateTime registrationDate;
    private final LocalDateTime lastLoginAt;
    private final RegistrationStatus registrationStatus;
}
