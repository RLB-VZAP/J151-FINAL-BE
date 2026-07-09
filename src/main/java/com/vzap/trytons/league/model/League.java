package com.vzap.trytons.league.model;

import com.vzap.trytons.league.enums.LeagueType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class League {

    private UUID leagueId;
    private String leagueName;
    private String description;
    private LeagueType leagueType;
    private String leagueCode;
    private LocalDateTime creationDate;
    private Boolean isActive;
    private int maxMembers;

    private RegisteredUser manager;

    private List<LeagueMembership> memberships = new ArrayList<>();
    private List<LeagueInvitation> invitations = new ArrayList<>();
    private List<Leaderboard> leaderboards = new ArrayList<>();
    private List<Fixture> fixtures = new ArrayList<>();
    private List<ScoringRule> scoringRules = new ArrayList<>();
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private List<SimulationSettings> simulationSettings = new ArrayList<>();
}