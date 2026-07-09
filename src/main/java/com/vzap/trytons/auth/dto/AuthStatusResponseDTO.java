package com.vzap.trytons.auth.dto;

import com.vzap.trytons.auth.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthStatusResponseDTO {

    private boolean authenticated;
    private UUID userId;
    private String username;
    private String email;
    private UserRole role;
}