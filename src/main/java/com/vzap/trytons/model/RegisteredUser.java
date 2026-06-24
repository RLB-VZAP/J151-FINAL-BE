package com.vzap.trytons.model;

import com.vzap.trytons.enums.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisteredUser extends User{

    public RegistrationStatus registrationStatus;
    public UUID userID;

}
