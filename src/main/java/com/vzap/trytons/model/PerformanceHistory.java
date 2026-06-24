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
public class PerformanceHistory {
    private UUID performanceHistoryId,playerId;
    private String season;
    private int roundNumber, matchesPlayed, tries, assists, tackles, fantasyPoints, formRating;
    private Date recordedAt;

}
