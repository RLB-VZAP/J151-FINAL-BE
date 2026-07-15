package com.vzap.trytons.model;

import com.vzap.trytons.enums.InvitationStatus;
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
    private InvitationStatus status;
    private LocalDateTime acceptedAt;
    private LocalDateTime createdAt;

    private UUID leagueId;
    private UUID invitedUserId;
    private UUID createdByUserId;
}