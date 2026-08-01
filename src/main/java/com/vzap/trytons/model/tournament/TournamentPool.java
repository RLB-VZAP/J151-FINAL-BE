package com.vzap.trytons.model.tournament;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class TournamentPool {
    private UUID poolId;
    private UUID tournamentId;
    private String poolName;
    private int poolSize;
    private LocalDateTime createdAt;
}
