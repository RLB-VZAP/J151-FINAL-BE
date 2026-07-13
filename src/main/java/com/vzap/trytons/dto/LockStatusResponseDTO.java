package com.vzap.trytons.dto;

import com.vzap.trytons.enums.FantasyRoundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LockStatusResponseDTO {
    //STUB
    private UUID roundId;
    private FantasyRoundStatus roundStatus;
    private List<UUID> lockedPlayerIds;
    private List<UUID> lockedTeamIds;
    private boolean locked;
    private boolean snapshotsCreated;
    private String message;
}
