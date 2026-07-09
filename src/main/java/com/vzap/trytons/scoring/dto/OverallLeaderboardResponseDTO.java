package com.vzap.trytons.scoring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class OverallLeaderboardResponseDTO {
    private String season;
    private List<LeaderboardEntryResponseDTO> standings;

}