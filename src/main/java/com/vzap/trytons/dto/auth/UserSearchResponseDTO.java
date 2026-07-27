package com.vzap.trytons.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

// Deliberately exposes only userId and username. This is the general-purpose
// (non-admin) user search used to find someone to message — it must never
// carry email or role, unlike AdminUserSearchResponseDTO.
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class UserSearchResponseDTO {
    private UUID userId;
    private String username;
}
