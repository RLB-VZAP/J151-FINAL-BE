package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeagueMemberResponseDTO {
    private UUID membershipId;
    private UUID userId;
    private UUID teamId;
    private LocalDateTime joinDate;
    private Boolean isActive;
}