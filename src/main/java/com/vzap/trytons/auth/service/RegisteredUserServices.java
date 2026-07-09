package com.vzap.trytons.auth.service;

import com.vzap.trytons.auth.dto.RegisteredUserRequestDTO;
import com.vzap.trytons.auth.model.RegisteredUser;

public interface RegisteredUserServices {
    RegisteredUser registerUser(RegisteredUserRequestDTO newUser);
}