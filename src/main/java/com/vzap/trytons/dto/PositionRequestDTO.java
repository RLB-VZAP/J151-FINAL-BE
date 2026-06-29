package com.vzap.trytons.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

public class PositionRequestDTO {
    private String positionName;
    private String positionCategory;
    private int minRequired;
    private int maxAllowed;
}
