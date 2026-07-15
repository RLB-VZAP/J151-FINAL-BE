package com.vzap.trytons.dto;

import com.vzap.trytons.enums.SquadRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class FantasyTeamPlayerSelectionRequestDTO {
    private UUID playerId;
    private SquadRole squadRole;
}
