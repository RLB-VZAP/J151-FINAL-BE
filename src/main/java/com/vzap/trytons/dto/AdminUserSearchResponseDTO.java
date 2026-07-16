package com.vzap.trytons.dto;

import com.vzap.trytons.enums.UserRole;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

public class AdminUserSearchResponseDTO {
    private UUID userId;
    private String email;
    private String username;
    private UserRole role;
    private Boolean isActive;
}
