package com.vzap.trytons.roster.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViewOpponentTeamDTO {
    private UUID teamId;
    private String teamName;
    private int totalPoints;
    private int weeklyPoints;
    private List<PlayerResponseDTO> players;
}
