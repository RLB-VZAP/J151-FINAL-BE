package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JoinLeagueResponseDTO {
    private String leagueName;
    private String message;
    private String description;
    private Boolean isActive;
    private int maxMembers;
}
