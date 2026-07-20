package com.vzap.trytons.dto.history;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

// TODO: This DTO has no fields and no endpoint currently returns it. Per the process inventory (E2E-13),
// TODO: only add player-specific history fields here and wire it to a dedicated endpoint if a player-level
// TODO: (as opposed to user-level) points history view is actually required; otherwise this type should be
// TODO: removed as redundant with UserPointsHistoryResponseDTO / WeeklyPerformanceResponseDTO.
public class PlayerPointsHistoryResponseDTO {

}
