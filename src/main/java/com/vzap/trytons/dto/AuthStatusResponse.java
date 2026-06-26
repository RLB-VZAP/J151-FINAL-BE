package com.vzap.trytons.dto;

import com.vzap.trytons.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthStatusResponse {

    private boolean authenticated;
    private UUID userId;
    private String username;
    private String email;
    private UserRole role;
}