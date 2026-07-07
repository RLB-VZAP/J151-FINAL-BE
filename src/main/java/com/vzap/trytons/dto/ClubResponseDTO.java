package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter

public class ClubResponseDTO {
    private UUID clubId;
    private String clubName;
    private String location;
    private String homeVenue;
    private int strengthRating;
    private boolean isActive;

}
