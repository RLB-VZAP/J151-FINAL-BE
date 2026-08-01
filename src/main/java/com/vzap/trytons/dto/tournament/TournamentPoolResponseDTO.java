package com.vzap.trytons.dto.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentPoolResponseDTO {
    private UUID poolId;
    private String poolName;
    private int poolSize;

    private List<TournamentStandingResponseDTO> standings;
}
