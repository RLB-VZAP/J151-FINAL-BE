package com.vzap.trytons.model;

import com.vzap.trytons.enums.RegistrationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisteredUser extends User {

    private RegistrationStatus registrationStatus;
}