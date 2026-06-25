package com.vzap.trytons.model;

import com.vzap.trytons.enums.TransferWindowStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Transfer {

    private UUID transferId;
    private LocalDateTime transferDate;
    private Boolean penaltyApplied;
    private int penaltyPoints;
    private TransferWindowStatus transferWindowStatus;
    private int roundNumber;
    private Boolean confirmed;

    private FantasyTeam fantasyTeam;
    private Player removedPlayer;
    private Player addedPlayer;
}