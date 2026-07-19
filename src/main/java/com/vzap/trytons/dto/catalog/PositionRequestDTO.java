package com.vzap.trytons.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class PositionRequestDTO {
    private String positionName;
    private String positionCategory;
    private int minRequired;
    private int maxAllowed;
}
