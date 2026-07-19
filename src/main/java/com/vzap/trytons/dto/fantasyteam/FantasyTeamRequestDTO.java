package com.vzap.trytons.dto.fantasyteam;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FantasyTeamRequestDTO {
    private String teamName;
    private List<FantasyTeamPlayerSelectionRequestDTO> selectedPlayers;
}
