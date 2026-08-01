package com.vzap.trytons.model.tournament;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class TournamentPoolMember {
    private UUID poolMemberId;
    private UUID tournamentId;
    private UUID poolId;
    private UUID teamId;
    private int seed;
    private LocalDateTime createdAt;
}
