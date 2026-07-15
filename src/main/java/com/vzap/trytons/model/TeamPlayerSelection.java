package com.vzap.trytons.model;

import com.vzap.trytons.enums.SquadRole;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TeamPlayerSelection {
    private UUID selectionId;
    private UUID teamId;
    private UUID playerId;
    private LocalDateTime selectedDate;
    private Boolean isCaptain;
    private Boolean is_vice_captain;
    private SquadRole squadRole;


}