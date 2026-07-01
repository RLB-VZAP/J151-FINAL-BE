package com.vzap.trytons.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
@Getter
public class SquadValidationResult {

    private final List<SquadValidationError> errors = new ArrayList<>();

    public boolean isValid(){
        return errors.isEmpty();
    }

    public void addError(String code, String message, String field){
        SquadValidationError newError = SquadValidationError.builder()
                .code(code)
                .message(message)
                .field(field)
                .build();
        errors.add(newError);
    }

}
