package com.vzap.trytons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamPlayerSelection {
    private UUID selectionId,teamId,playerId;
    private Date selectedDate;
    private boolean isCaptain,isViceCaptain,isActive;

}
