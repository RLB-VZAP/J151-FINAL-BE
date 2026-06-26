package com.vzap.trytons.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegisteredUserRequest {
    @NotBlank(message = "Email can not be blank")
    @Email(message = "Must be a vailed Email")
    @Size(min = 1, max = 255)
    private String email;

    @NotBlank(message = "Username can not be blank")
    private String username;

    @NotBlank(message = "Password can not be blank")
    @Size(min = 8, message = "Password needs to be atleast 8 characters long")
    private String rawPassword;
}
