package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PositionResponseDTO {
    private UUID positionId;
    private String positionName;
    private String positionCategory;
    private int minRequired;
    private int maxAllowed;
}
