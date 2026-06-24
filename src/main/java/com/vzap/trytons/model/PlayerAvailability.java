package com.vzap.trytons.model;

import com.vzap.trytons.enums.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerAvailability {
    private UUID availabilityId,playerId;
    private AvailabilityStatus availabilityStatus;
    private Date effectiveDate,endDate;
    private String notes;
}
