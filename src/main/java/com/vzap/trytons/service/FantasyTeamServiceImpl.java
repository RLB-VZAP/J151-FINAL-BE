package com.vzap.trytons.service;

import com.vzap.trytons.dao.*;
import com.vzap.trytons.dto.*;
import com.vzap.trytons.enums.SquadRole;
import com.vzap.trytons.exceptions.BadRequestException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Player;
import com.vzap.trytons.model.TeamPlayerSelection;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FantasyTeamServiceImpl implements FantasyTeamService {
    private static final BigDecimal INITIAL_BUDGET = BigDecimal.valueOf(100000000.00);

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Inject
    private FantasyTeamPlayerDAO fantasyTeamPlayerDAO;

    @Inject
    private RegisteredUserDAO registeredUserDAO;

    @Inject
    private PlayerDAO playerDAO;

    @Override
    public FantasyTeamResponseDTO createTeam(UUID registeredUserId, FantasyTeamRequestDTO request) {
        FantasyTeam fantasyTeam = mapRequestToFantasyTeam(request);

        fantasyTeam.setTeamId(UUID.randomUUID());
        fantasyTeam.setOwner(registeredUserDAO.getRegisteredUserById(registeredUserId).orElseThrow(() -> new RuntimeException("Register User not found.")));
        fantasyTeam.setTotalTeamValue(totalTeamValue(request));
        fantasyTeam.setRemainingBudget(INITIAL_BUDGET);
        fantasyTeam.setCreationDate(LocalDateTime.now());
        fantasyTeam.setTotalPoints(0);
        fantasyTeam.setWeeklyPoints(0);
        fantasyTeam.setIsValid(true);
        fantasyTeam.setIsLocked(false);

        List<FantasyTeamPlayerSelectionRequestDTO> selectedRequestPlayers = request.getSelectedPlayers();
        List<FantasyTeamPlayerSelectionResponseDTO> selectedResponsePlayers = new ArrayList<>();
        for (FantasyTeamPlayerSelectionRequestDTO requestPlayers : selectedRequestPlayers){
            Player player = playerDAO.getPlayerById(requestPlayers.getPlayerId()).orElseThrow(() -> new RuntimeException("Player Not Found."));
            selectedResponsePlayers.add(FantasyTeamPlayerSelectionResponseDTO.builder()
                    .playerId(player.getPlayerId())
                    .playerName(player.getPlayerName())
                    .positionId(player.getPosition().getPositionId())
                    .positionName(player.getPosition().getPositionName())
                    .clubId(player.getClub().getClubId())
                    .clubName(player.getClub().getClubName())
                    .value(player.getValue())
                    .isActive(player.isActive())
                    .totalFantasyPoints(player.getTotalFantasyPoints())
                    .currentForm(player.getCurrentForm())
                    .build());
        }

        return FantasyTeamResponseDTO.builder()
                .teamId(fantasyTeam.getTeamId())
                .teamName(fantasyTeam.getTeamName())
                .managerId(fantasyTeam.getOwner().getUserId())
                .managerUsername(fantasyTeam.getOwner().getUsername())
                .totalTeamValue(fantasyTeam.getTotalTeamValue())
                .remainingBudget(fantasyTeam.getRemainingBudget())
                .weeklyPoints(fantasyTeam.getWeeklyPoints())
                .totalPoints(fantasyTeam.getTotalPoints())
                .valid(fantasyTeam.getIsValid())
                .locked(fantasyTeam.getIsLocked())
                .selectedPlayers(selectedResponsePlayers)
                .build();
    }

    @Override
    public ViewOpponentTeamDTO viewOpponentTeam(UUID teamId) {
        FantasyTeam fantasyTeam = fantasyTeamDAO.findTeamById(teamId);
        PlayerResponseDTO playerResponseDTO;
        List<PlayerResponseDTO> playerResponses = new ArrayList<>();
        Player player;
        List<TeamPlayerSelection> squad = fantasyTeamPlayerDAO.getSquadByTeamId(teamId);
        for(TeamPlayerSelection squadPlayer : squad){

            player = squadPlayer.getPlayer();

            playerResponseDTO = PlayerResponseDTO.builder()
                    .playerId(player.getPlayerId())
                    .playerName(player.getPlayerName())
                    .value(player.getValue())
                    .attackingAbility(player.getAttackingAbility())
                    .defensiveAbility(player.getDefensiveAbility())
                    .kickingAbility(player.getKickingAbility())
                    .discipline(player.getDiscipline())
                    .consistency(player.getConsistency())
                    .fitness(player.getFitness())
                    .currentForm(player.getCurrentForm())
                    .totalFantasyPoints(player.getTotalFantasyPoints())
                    .isActive(player.isActive())
                    .club(player.getClub())
                    .position(player.getPosition())
                    .isCaptain(squadPlayer.getIsCaptain())
                    .isViceCaptain(squadPlayer.getIsViceCaptain())
                    .isBench(squadPlayer.getSquadRole() == SquadRole.BENCH)
                    .build();
            playerResponses.add(playerResponseDTO);
        }

        return ViewOpponentTeamDTO.builder()
                .teamId(teamId)
                .teamName(fantasyTeam.getTeamName())
                .totalPoints(fantasyTeam.getTotalPoints())
                .weeklyPoints(fantasyTeam.getWeeklyPoints())
                .players(playerResponses)
                .build();
    }

    @Override
    public ViewOwnTeamDTO viewOwnTeam(UUID registeredUserId, UUID teamId) {
        FantasyTeam fantasyTeam = fantasyTeamDAO.findTeamById(teamId);
        List<TeamPlayerSelection> playerResponses = fantasyTeamPlayerDAO.getSquadByTeamId(teamId);
        List<PlayerResponseDTO> playerResponsesDTO = new ArrayList<>();
        for(TeamPlayerSelection playerResponse : playerResponses){
            Player player = playerResponse.getPlayer();
            playerResponsesDTO.add(PlayerResponseDTO.builder()
                    .playerId(player.getPlayerId())
                    .playerName(player.getPlayerName())
                    .value(player.getValue())
                    .attackingAbility(player.getAttackingAbility())
                    .defensiveAbility(player.getDefensiveAbility())
                    .kickingAbility(player.getKickingAbility())
                    .discipline(player.getDiscipline())
                    .consistency(player.getConsistency())
                    .fitness(player.getFitness())
                    .currentForm(player.getCurrentForm())
                    .totalFantasyPoints(player.getTotalFantasyPoints())
                    .isActive(player.isActive())
                    .club(player.getClub())
                    .position(player.getPosition())
                    .isCaptain(player.isActive())
                    .isViceCaptain(player.isActive())
                    .isBench(playerResponse.getSquadRole() == SquadRole.BENCH)
                    .build());
        }
        return ViewOwnTeamDTO.builder()
                .teamId(fantasyTeam.getTeamId())
                .teamName(fantasyTeam.getTeamName())
                .totalTeamValue(fantasyTeam.getTotalTeamValue())
                .remainingBudget(fantasyTeam.getRemainingBudget())
                .creationDate(fantasyTeam.getCreationDate())
                .totalPoints(fantasyTeam.getTotalPoints())
                .weeklyPoints(fantasyTeam.getWeeklyPoints())
                .isValid(fantasyTeam.getIsValid())
                .isLocked(fantasyTeam.getIsLocked())
                .ownerUsername(fantasyTeam.getOwner().getUsername())
                .players(playerResponsesDTO)
                .build();
    }

    @Override
    public FantasyTeamResponseDTO updateTeam(UUID registeredId, UUID teamId, FantasyTeamRequestDTO fantasyTeamDTO) {

        return null;
    }

    private FantasyTeam mapRequestToFantasyTeam(FantasyTeamRequestDTO request) {
        FantasyTeam fantasyTeam = new FantasyTeam();
        fantasyTeam.setTeamName(request.getTeamName().trim());
        return fantasyTeam;
    }

    private BigDecimal totalTeamValue(FantasyTeamRequestDTO request) {
        BigDecimal teamValue = BigDecimal.ZERO;
        try {
            for(FantasyTeamPlayerSelectionRequestDTO playerSelectionDTO : request.getSelectedPlayers()) {
                Player player = playerDAO.getPlayerById(playerSelectionDTO.getPlayerId()).orElseThrow(() -> new RuntimeException("Player not found."));
                teamValue = teamValue.add(player.getValue());
            }
            return teamValue;
        }catch(RuntimeException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
