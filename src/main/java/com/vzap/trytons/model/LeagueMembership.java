package com.vzap.trytons.model;

import com.vzap.trytons.enums.LeagueMemberRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class LeagueMembership {

    private UUID membershipId;
    private Boolean isActive;
    private LocalDateTime joinDate;

    private League league;
    private RegisteredUser registeredUser;
    private FantasyTeam fantasyTeam;
}