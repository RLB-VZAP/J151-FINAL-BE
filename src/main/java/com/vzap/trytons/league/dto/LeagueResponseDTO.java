package com.vzap.trytons.league.dto;

import com.vzap.trytons.league.enums.LeagueType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeagueResponseDTO {
    private UUID leagueId;
    private UUID managerUserId;
    private String leagueName;
    private String description;
    private LeagueType leagueType;
    private LocalDateTime creationDate;
    private Boolean isActive;
    private int maxMembers;
}
