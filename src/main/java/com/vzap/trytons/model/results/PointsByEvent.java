package com.vzap.trytons.model.results;

import lombok.*;

/**
 * How much of a team's fantasy total came from one kind of match event.
 *
 * <p>Aggregated across every player in the team for a single match result, so
 * a fixture can show that (say) 30 of its 190 points came from six tries.
 */
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class PointsByEvent {
    private String eventType;
    private String description;
    private int eventCount;
    /** Already signed: a deduction such as a missed tackle arrives negative. */
    private int pointsEarned;
    private boolean deduction;
}
