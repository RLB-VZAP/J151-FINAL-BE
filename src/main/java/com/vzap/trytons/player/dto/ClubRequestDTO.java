package com.vzap.trytons.player.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class ClubRequestDTO {
    private String clubName;
    private String location;
    private String homeVenue;
    private boolean isActive;
}
