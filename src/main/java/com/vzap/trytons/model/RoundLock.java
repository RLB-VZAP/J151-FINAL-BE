package com.vzap.trytons.model;

import com.vzap.trytons.enums.RoundLockAction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class RoundLock {
    private UUID lockId;
    private UUID roundId;
    private RoundLockAction lockAction;
    private UUID actionByAdminUserId;
    private LocalDateTime actionAt;
    private String reason;
}