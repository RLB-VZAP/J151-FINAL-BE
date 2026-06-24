package com.vzap.trytons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerStatistics {
    private UUID statId,fixtureId, playerId,capturedByAdminUserId;
    private int tries,assists, tackles, missedTackles, conversions, penalties, metersGained, yellowCards, redCards;
    private Date statisticDate;

}
