package com.vzap.trytons.dto.leaderboard;

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