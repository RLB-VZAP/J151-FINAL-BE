package com.vzap.trytons.service;

import com.vzap.trytons.dto.RegisteredUserRequest;
import com.vzap.trytons.model.RegisteredUser;

public interface RegisteredUserServices {
    RegisteredUser registerUser(RegisteredUserRequest newUser);
}