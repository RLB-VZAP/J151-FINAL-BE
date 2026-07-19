package com.vzap.trytons.service.auth;

import com.vzap.trytons.dto.auth.RegisteredUserRequestDTO;
import com.vzap.trytons.model.auth.RegisteredUser;

public interface RegisteredUserServices {
    RegisteredUser registerUser(RegisteredUserRequestDTO newUser);
}