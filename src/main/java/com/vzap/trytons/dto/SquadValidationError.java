package com.vzap.trytons.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SquadValidationError {
    private String code;
    private String message;
    private String field;
}
