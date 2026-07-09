package com.vzap.trytons.simulation.dto;

import com.vzap.trytons.simulation.enums.FantasyRoundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeadlineStatusResponseDTO {
    //STUB
    private UUID roundId;
    private FantasyRoundStatus roundStatus;
    private LocalDateTime openDate;
    private LocalDateTime lockDeadline;
    private LocalDateTime endDate;
    private boolean locked;
    private boolean openForTransfers;
    private String message;
}
