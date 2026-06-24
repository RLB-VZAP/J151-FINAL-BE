package com.vzap.trytons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Club {
    private UUID clubID;
    private String clubName, location, homeVenue;
    private int strengthRating;
    private boolean isActive;

}
