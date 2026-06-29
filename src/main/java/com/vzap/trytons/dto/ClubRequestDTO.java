package com.vzap.trytons.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

public class ClubRequestDTO {
    private String clubName;
    private String location;
    private String homeVenue;
    private int strengthRating;
    private boolean isActive;
}
