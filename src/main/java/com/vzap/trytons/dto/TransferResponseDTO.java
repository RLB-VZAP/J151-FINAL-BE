package com.vzap.trytons.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponseDTO {
    private  UUID transferId, teamId, roundId, removed_player_id, added_player_id;
    private String removed_player_name, added_player_name;
    private BigDecimal removed_player_value, added_player_value, valueDifference;
    private int penaltyPoints;
    private String status;
    private LocalDateTime transferDate, confirmationDate;

}
