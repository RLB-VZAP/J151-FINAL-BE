package com.vzap.trytons.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Locking {

    private UUID lockId;
    private LocalDateTime lockedAt;
    private LocalDateTime unlockAt;
    private Boolean isLocked;
    private String reason;

    private Fixture fixture;

    private FantasyTeam fantasyTeam;
    private Player player;
    private Administrator lockedByAdmin;
    private Transfer transfer;
}