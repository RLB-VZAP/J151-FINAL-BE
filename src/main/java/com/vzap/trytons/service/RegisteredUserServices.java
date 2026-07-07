package com.vzap.trytons.service;

import com.vzap.trytons.dto.RegisteredUserRequestDTO;
import com.vzap.trytons.model.RegisteredUser;

public interface RegisteredUserServices {
    RegisteredUser registerUser(RegisteredUserRequestDTO newUser);
}