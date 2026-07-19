package com.vzap.trytons.dto.fantasyteam;

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
