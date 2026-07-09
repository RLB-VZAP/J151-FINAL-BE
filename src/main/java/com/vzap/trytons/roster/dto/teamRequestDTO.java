package com.vzap.trytons.roster.dto;

import com.vzap.trytons.player.dto.PlayerRequestDTO;

import java.util.List;

public class teamRequestDTO {
    private String teamName;
    private List<PlayerRequestDTO> selectedPlayers;
}