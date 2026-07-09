package com.vzap.trytons.auth.dto;

import com.vzap.trytons.auth.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private UUID userId;
    private String username;
    private String email;
    private UserRole role;
    private String token;
}