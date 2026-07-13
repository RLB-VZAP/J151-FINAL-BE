package com.vzap.trytons.model;

import com.vzap.trytons.enums.SquadRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class TeamPlayerSelection {
    private UUID selectionId;

    private LocalDateTime selectedDate;

    private Boolean isCaptain;
    private Boolean isViceCaptain;

    private SquadRole squadRole;

    private FantasyTeam fantasyTeam;

    private Player player;
}