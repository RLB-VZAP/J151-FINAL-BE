package com.vzap.trytons.dto;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class PlayerStatisticsCorrectionRequestDTO {
    private UUID statId;
    private UUID correctionByAdminUserId;
    private String reason;
    // Old values are read from the database (captured server-side), not supplied by the client.
    private Map<String, Object> newValuesJson;
}
