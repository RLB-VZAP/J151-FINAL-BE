package com.vzap.trytons.fixture.model;

import com.vzap.trytons.fixture.enums.MatchTeamSide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchTeamScore {
    private UUID teamScoreId;
    private UUID resultId;
    private UUID teamId;
    private MatchTeamSide teamSide;
    private int score;
}