package com.vzap.trytons.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

public class ClubResponseDTO {
    private UUID clubId;
    private String clubName;
    private String location;
    private String homeVenue;
    private int strengthRating;
    private boolean isActive;

}
