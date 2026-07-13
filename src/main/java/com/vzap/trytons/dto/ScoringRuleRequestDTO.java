package com.vzap.trytons.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScoringRuleRequestDTO {

    @NotBlank(message = "Event type is required")
    @Size(max = 50, message = "Event type cannot exceed 50 characters")
    private String eventType;
    private int pointsValue;
    @NotBlank(message = "Season is required")
    @Size(max = 20, message = "Season cannot exceed 20 characters")
    private String season;
    private boolean active;
}