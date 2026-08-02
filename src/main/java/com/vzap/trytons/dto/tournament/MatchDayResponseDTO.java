package com.vzap.trytons.dto.tournament;

import com.vzap.trytons.enums.TournamentStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Where a round ended up after being moved.
 *
 * <p>{@code stageLabel} is carried as an explicit string because Jackson
 * serialises an enum by {@code name()}: a getter on {@link TournamentStage}
 * alone never reaches the wire.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDayResponseDTO {

    private UUID roundId;
    private Integer matchdayNumber;

    private TournamentStage stage;
    /** Human readable stage, e.g. "Quarter-Finals". */
    private String stageLabel;

    private LocalDate matchDay;
    private LocalTime kickoff;

    /** How many fixtures were moved with the round. */
    private int fixturesMoved;
}
