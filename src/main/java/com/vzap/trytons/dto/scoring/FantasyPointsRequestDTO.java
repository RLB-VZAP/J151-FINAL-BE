package com.vzap.trytons.dto.scoring;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FantasyPointsRequestDTO {

    private UUID statId;
}