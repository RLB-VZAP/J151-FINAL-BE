package com.vzap.trytons.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class ClubRequestDTO {
    private String clubName;
    private String location;
    private String homeVenue;
    @JsonProperty("isActive")
    private boolean isActive;
}
