package com.vzap.trytons.roster.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SquadValidationErrorDTO {
    private String code;
    private String message;
    private String field;
}
