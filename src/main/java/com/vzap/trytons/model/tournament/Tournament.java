package com.vzap.trytons.model.tournament;

import com.vzap.trytons.enums.TournamentStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Tournament {
    private UUID tournamentId;
    private UUID leagueId;
    private String season;

    private TournamentStatus status;

    private int managerCount;
    private int poolCount;
    private int poolMatchdays;
    private int bracketSize;
    private boolean thirdPlacePlayoff;

    private UUID championTeamId;
    private UUID runnerUpTeamId;
    private UUID thirdPlaceTeamId;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
