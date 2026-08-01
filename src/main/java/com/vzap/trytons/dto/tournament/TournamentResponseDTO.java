package com.vzap.trytons.dto.tournament;

import com.vzap.trytons.enums.TournamentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentResponseDTO {
    private UUID tournamentId;
    private UUID leagueId;
    private String leagueName;
    private String season;

    private TournamentStatus status;

    private int managerCount;
    private int poolCount;
    private int poolMatchdays;
    private int bracketSize;
    private boolean thirdPlacePlayoff;

    private UUID championTeamId;
    private String championTeamName;
    private UUID runnerUpTeamId;
    private String runnerUpTeamName;
    private UUID thirdPlaceTeamId;
    private String thirdPlaceTeamName;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    private List<TournamentPoolResponseDTO> pools;
}
