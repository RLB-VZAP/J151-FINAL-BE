package com.vzap.trytons.roster.model;

import com.vzap.trytons.player.model.Player;
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

    private FantasyTeam fantasyTeam;

    private Player player;
}