package com.vzap.trytons.roster.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class FantasyTeamPlayerSelectionRequestDTO {
    private UUID playerId;
}
