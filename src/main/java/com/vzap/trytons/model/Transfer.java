package com.vzap.trytons.model;

import com.vzap.trytons.enums.TransferStatus;
import com.vzap.trytons.enums.TransferWindowStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Transfer {
    private UUID transferId;
    private LocalDateTime transferDate, confirmationDate;
    private int penaltyPoints;
    private BigDecimal removed_player_value, added_player_value, valueDifference;
    private TransferStatus status;
    private FantasyRound round;
    private FantasyTeam fantasyTeam;
    private Player removedPlayer;
    private Player addedPlayer;
    private RegisteredUser createdBy;



}