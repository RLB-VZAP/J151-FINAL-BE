package com.vzap.trytons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Position {
    private UUID positionID;
    private String positionName, positionCategory;
    private int minRequired,maxAllowed;

}
