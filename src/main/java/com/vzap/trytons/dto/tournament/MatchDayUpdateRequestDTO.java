package com.vzap.trytons.dto.tournament;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Moves one fantasy round -- and therefore every fixture in it -- to another
 * match day.
 *
 * <p>The editable unit is deliberately the round rather than the individual
 * fixture: "all the fixtures of a round are played on the same day" then holds
 * by construction rather than by everyone remembering to keep them in step.
 *
 * <p>The kickoff moves with it. A round's kickoff is stored as its
 * {@code lockDeadline}, and every fixture reads its date and time from there,
 * so changing it here moves the whole round and the two cannot drift apart.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDayUpdateRequestDTO {

    /**
     * Must be one of the days rugby is played on -- Monday, Wednesday, Friday,
     * Saturday or Sunday -- and still in the future.
     */
    private LocalDate matchDay;

    /**
     * Kickoff for every fixture in the round. Optional: left out, the round
     * keeps the time it already has.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm[:ss]")
    private LocalTime kickoff;
}
