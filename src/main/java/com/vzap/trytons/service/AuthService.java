package com.vzap.trytons.service;

import com.vzap.trytons.dto.LoginResponse;

public interface AuthService {
    LoginResponse authenticate(String identifier, String password);

}
