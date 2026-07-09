package com.vzap.trytons.league.dto;

import com.vzap.trytons.league.enums.LeagueType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeagueRequestDTO {
    private UUID managerUserId;
    private String leagueName;
    private String description;
    private LeagueType leagueType;
    private int maxMembers;
}
