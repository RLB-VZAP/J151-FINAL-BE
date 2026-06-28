package com.vzap.trytons.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegisteredUserRequest {
    @NotBlank(message = "Email can not be blank")
    @Pattern(regexp = "\\S+", message = "Email cannot contain spaces")
    @Email(message = "Must be a valid Email")
    @Size(max = 255, message = "Email cannot be more than 255 characters")
    private String email;

    @NotBlank(message = "Username can not be blank")
    @Pattern(regexp = "\\S+", message = "Username cannot contain spaces")
    @Size(min = 5, max = 100)
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password needs to be at least 8 characters long")
    private String rawPassword;
}
