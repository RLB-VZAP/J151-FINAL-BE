package com.vzap.trytons.model;

import com.vzap.trytons.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    private UUID userId;
    private String email;
    private String passwordHash;
    private String username;
    private UserRole role;
    private Boolean isActive;
    private String profilePic;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginAt;
}