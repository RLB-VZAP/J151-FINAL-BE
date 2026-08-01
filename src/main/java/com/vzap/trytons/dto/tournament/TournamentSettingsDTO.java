package com.vzap.trytons.dto.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentSettingsDTO {
    private UUID settingsId;

    private int winPoints;
    private int drawPoints;
    private int lossPoints;

    private int attackBonusThreshold;
    private int losingBonusMargin;

    private boolean thirdPlacePlayoff;

    private LocalDateTime updatedAt;
}
