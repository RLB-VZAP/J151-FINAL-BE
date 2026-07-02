package com.vzap.trytons.security;

import com.vzap.trytons.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;
@Getter
@AllArgsConstructor
public class AuthPrincipal {
    private UUID userId;
    private String username;
    private String email;
    private UserRole role;

}
