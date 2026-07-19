package com.vzap.trytons.dto.fantasyteam;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class SquadValidationResultDTO{

    private final List<SquadValidationErrorDTO> errors = new ArrayList<>();

    public boolean isValid(){
        return errors.isEmpty();
    }

    public void addError(String code, String message, String field){
        SquadValidationErrorDTO newError = SquadValidationErrorDTO.builder()
                .code(code)
                .message(message)
                .field(field)
                .build();
        errors.add(newError);
    }

}
