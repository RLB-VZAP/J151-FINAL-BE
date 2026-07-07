package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor


public class PositionResponseDTO {
    private UUID positionId;
    private String positionName;
    private String positionCategory;
    private int minRequired;
    private int maxAllowed;
}
