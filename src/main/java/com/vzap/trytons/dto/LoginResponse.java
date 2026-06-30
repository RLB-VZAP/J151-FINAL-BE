package com.vzap.trytons.dto;

import com.vzap.trytons.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private UUID userId;
    private String username;
    private String email;
    private UserRole role;
    private String token;
}