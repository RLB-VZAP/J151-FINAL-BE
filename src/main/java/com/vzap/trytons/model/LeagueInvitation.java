package com.vzap.trytons.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class LeagueInvitation {

    private UUID invitationId;
    private LocalDateTime expiryDate;
    private Boolean expired;
    private LocalDateTime acceptedAt;

    private League league;
}