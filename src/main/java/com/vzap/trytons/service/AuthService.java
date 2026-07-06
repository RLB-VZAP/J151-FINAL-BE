package com.vzap.trytons.service;

import com.vzap.trytons.dto.AuthStatusResponseDTO;
import com.vzap.trytons.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO authenticate(String identifier, String password);
    String logout();
    AuthStatusResponseDTO getAuthStatus(String requestingUserId);
}
