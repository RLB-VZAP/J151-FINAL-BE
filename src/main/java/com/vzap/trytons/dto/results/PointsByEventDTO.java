package com.vzap.trytons.dto.results;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsByEventDTO {
    private String eventType;
    private String description;
    private int eventCount;
    /** Signed, so deductions render as negative without further work. */
    private int pointsEarned;
    private boolean deduction;
}
