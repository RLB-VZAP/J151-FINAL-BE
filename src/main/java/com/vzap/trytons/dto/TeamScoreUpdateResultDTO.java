package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TeamScoreUpdateResultDTO {

    private String fixtureId;
    private String teamId;
    private int teamATotal;
    private int teamBTotal;
    private String outcome;

}
