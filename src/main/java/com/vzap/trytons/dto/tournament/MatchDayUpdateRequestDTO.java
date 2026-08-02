package com.vzap.trytons.dto.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Moves one fantasy round -- and therefore every fixture in it -- to another
 * match day.
 *
 * <p>The editable unit is deliberately the round rather than the individual
 * fixture: "all the fixtures of a round are played on the same day" then holds
 * by construction rather than by everyone remembering to keep them in step.
 *
 * <p>Kickoff is not editable. It is fixed at {@code MatchdayCalendar.KICKOFF}
 * so a fixture's date always derives from its round's lock deadline.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDayUpdateRequestDTO {

    /** Must be a Wednesday, Saturday or Sunday, and still in the future. */
    private LocalDate matchDay;
}
