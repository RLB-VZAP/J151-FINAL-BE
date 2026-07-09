package com.vzap.trytons.auth.model;

import com.vzap.trytons.auth.enums.RegistrationStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RegisteredUser extends User {
    private RegistrationStatus registrationStatus;
}