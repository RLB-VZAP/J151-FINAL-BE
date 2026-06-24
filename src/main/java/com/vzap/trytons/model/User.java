package com.vzap.trytons.model;

import com.vzap.trytons.enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private UUID userID;
    private String email;
    private String passwordHash;
    private String userName;
    private UserRole role;
    private boolean isActive;
    private String profilePic;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginAt;
}
