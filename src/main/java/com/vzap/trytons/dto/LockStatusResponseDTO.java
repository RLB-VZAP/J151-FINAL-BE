package com.vzap.trytons.dto;

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
    private UUID fixtureId;
    private List<UUID>LockedPlayerIds;
    private List<UUID>LockedTeamIds;
    private boolean locked;
    private String message;
}
