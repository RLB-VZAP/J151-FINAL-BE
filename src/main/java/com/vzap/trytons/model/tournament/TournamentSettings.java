package com.vzap.trytons.model.tournament;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class TournamentSettings {
    private UUID settingsId;

    private int winPoints;
    private int drawPoints;
    private int lossPoints;

    /** Fantasy-points equivalent of the Rugby World Cup four-try bonus. */
    private int attackBonusThreshold;
    /** Fantasy-points equivalent of the Rugby World Cup losing bonus. */
    private int losingBonusMargin;

    private boolean thirdPlacePlayoff;

    private LocalDateTime updatedAt;
}
