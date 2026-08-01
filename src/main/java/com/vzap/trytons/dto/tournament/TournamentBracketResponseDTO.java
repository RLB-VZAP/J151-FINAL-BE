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
public class TournamentBracketResponseDTO {
    private UUID tournamentId;
    private int bracketSize;
    private boolean thirdPlacePlayoff;

    private List<TournamentFixtureResponseDTO> fixtures;
}
