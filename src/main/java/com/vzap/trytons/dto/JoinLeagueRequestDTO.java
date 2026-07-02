package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JoinLeagueRequestDTO {
    private String leagueCode; // Will be null for public leagues
    private UUID leagueId;
}
