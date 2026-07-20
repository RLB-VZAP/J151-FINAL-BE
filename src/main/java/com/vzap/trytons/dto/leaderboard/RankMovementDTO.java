package com.vzap.trytons.dto.leaderboard;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

// TODO: This DTO has no fields and no endpoint currently returns it. Rank movement is already carried
// TODO: inline on LeaderboardEntryResponseDTO (rankMovement, previousRanking). Decide whether this class
// TODO: should be removed as redundant, or filled in and wired to a dedicated rank-movement endpoint if
// TODO: one is required - do not leave it as a fieldless, unused type.
public class RankMovementDTO {

}
