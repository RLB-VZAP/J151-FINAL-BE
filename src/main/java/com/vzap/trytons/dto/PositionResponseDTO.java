package com.vzap.trytons.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

public class PositionResponseDTO {
    private UUID positionId;
    private String positionName;
    private String positionCategory;
    private int minRequired;
    private int maxAllowed;
}
