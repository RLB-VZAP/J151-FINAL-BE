package com.vzap.trytons.model;

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
public class FantasyTeamRoundSelection{
    private UUID selectionId;
    private UUID roundId;
    private UUID teamId;
    private UUID playerId;
    private LocalDateTime selectedDate;
    private boolean isCaptain;
    private boolean isViceCaptain;
    private LocalDateTime lockedAt;
}