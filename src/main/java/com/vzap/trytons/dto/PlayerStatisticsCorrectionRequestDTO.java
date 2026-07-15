package com.vzap.trytons.dto;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class PlayerStatisticsCorrectionRequestDTO {
    private UUID statId;
    private UUID correctionByAdminUserId;
    private String reason;
    private Map<String, Object> oldValueJason;
    private Map<String, Object> newValueJason;
}
