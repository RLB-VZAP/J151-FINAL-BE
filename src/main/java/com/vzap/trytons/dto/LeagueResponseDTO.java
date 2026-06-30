package com.vzap.trytons.dto;

import com.vzap.trytons.enums.LeagueType;

import java.time.LocalDateTime;
import java.util.UUID;

public class LeagueResponseDTO {

    private UUID leagueId;
    private String leagueName;
    private String description;
    private LeagueType leagueType;
    private String leagueCode;
    private LocalDateTime creationDate;
    private Boolean isActive;
    private int maxMembers;
    private UUID membershipId;

}
