package com.vzap.trytons.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStatisticsCorrection {
    private UUID correctionId;
    private UUID statId;
    private UUID correctionByAdminUserId;
    private String reason;
    private Map<String , Object > oldValueJason;
    private Map<String , Object > newValueJason;
    private LocalDateTime correctionTime;
}
