package com.vzap.trytons.auth.service;

import com.vzap.trytons.auth.dto.AuthStatusResponseDTO;
import com.vzap.trytons.auth.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO authenticate(String identifier, String password);
    String logout();
    AuthStatusResponseDTO getAuthStatus(String requestingUserId);
}
