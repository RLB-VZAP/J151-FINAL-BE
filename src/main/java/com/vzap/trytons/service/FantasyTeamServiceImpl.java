package com.vzap.trytons.service;

import com.vzap.trytons.dao.*;
import com.vzap.trytons.dto.*;
import com.vzap.trytons.enums.SquadRole;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
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


    @Inject
    private SquadValidationService squadValidationService;
    @Override
    public FantasyTeamResponseDTO createTeam(UUID registeredUserId, FantasyTeamRequestDTO request) {
        FantasyTeam fantasyTeam = mapRequestToFantasyTeam(request);

        fantasyTeam.setTeamId(UUID.randomUUID());
        // TODO: FantasyTeam.owner (RegisteredUser) replaced by ownerUserId (UUID) — model now mirrors schema.sql
        fantasyTeam.setOwner(registeredUserDAO.getRegisteredUserById(registeredUserId).orElseThrow(() -> new RuntimeException("Register User not found.")));
        fantasyTeam.setTotalTeamValue(totalTeamValue(request));
        fantasyTeam.setRemainingBudget(INITIAL_BUDGET);
        fantasyTeam.setCreationDate(LocalDateTime.now());
        fantasyTeam.setTotalPoints(0);
        // TODO: FantasyTeam.weeklyPoints/isLocked removed (no column, no derivation) — model now mirrors schema.sql
        fantasyTeam.setWeeklyPoints(0);
        fantasyTeam.setIsValid(true);
        // TODO: FantasyTeam.isLocked removed (no column, no derivation) — model now mirrors schema.sql
        fantasyTeam.setIsLocked(false);

        List<FantasyTeamPlayerSelectionRequestDTO> selectedRequestPlayers = request.getSelectedPlayers();
        List<FantasyTeamPlayerSelectionResponseDTO> selectedResponsePlayers = new ArrayList<>();
        for (FantasyTeamPlayerSelectionRequestDTO requestPlayers : selectedRequestPlayers){
            Player player = playerDAO.getPlayerById(requestPlayers.getPlayerId()).orElseThrow(() -> new RuntimeException("Player Not Found."));
            selectedResponsePlayers.add(FantasyTeamPlayerSelectionResponseDTO.builder()
                    .playerId(player.getPlayerId())
                    .playerName(player.getPlayerName())
                    // TODO: Player.position/club replaced by positionId/clubId (UUID FK) — model now mirrors schema.sql
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
                // TODO: FantasyTeam.owner (RegisteredUser) replaced by ownerUserId (UUID) — model now mirrors schema.sql
                .managerId(fantasyTeam.getOwner().getUserId())
                .managerUsername(fantasyTeam.getOwner().getUsername())
                .totalTeamValue(fantasyTeam.getTotalTeamValue())
                .remainingBudget(fantasyTeam.getRemainingBudget())
                // TODO: FantasyTeam.weeklyPoints/isLocked removed (no column, no derivation) — model now mirrors schema.sql
                .weeklyPoints(fantasyTeam.getWeeklyPoints())
                .totalPoints(fantasyTeam.getTotalPoints())
                .valid(fantasyTeam.getIsValid())
                // TODO: FantasyTeam.isLocked removed (no column, no derivation) — model now mirrors schema.sql
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

            // TODO: TeamPlayerSelection.player replaced by playerId (UUID FK) — model now mirrors schema.sql
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
                    // TODO: Player.club/position replaced by clubId/positionId (UUID FK) — model now mirrors schema.sql
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
                // TODO: FantasyTeam.weeklyPoints removed (no column, no derivation) — model now mirrors schema.sql
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
            // TODO: TeamPlayerSelection.player replaced by playerId (UUID FK) — model now mirrors schema.sql
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
                    // TODO: Player.club/position replaced by clubId/positionId (UUID FK) — model now mirrors schema.sql
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
                // TODO: FantasyTeam.weeklyPoints/isLocked removed (no column, no derivation) — model now mirrors schema.sql
                .weeklyPoints(fantasyTeam.getWeeklyPoints())
                .isValid(fantasyTeam.getIsValid())
                // TODO: FantasyTeam.isLocked removed (no column, no derivation); FantasyTeam.owner (RegisteredUser) replaced by ownerUserId (UUID) — model now mirrors schema.sql
                .isLocked(fantasyTeam.getIsLocked())
                .ownerUsername(fantasyTeam.getOwner().getUsername())
                .players(playerResponsesDTO)
                .build();
    }

    @Override
    public FantasyTeamResponseDTO updateTeam(UUID registeredId, UUID teamId, FantasyTeamRequestDTO fantasyTeamDTO) {
        FantasyTeam fantasyTeam = fantasyTeamDAO.getTeamById(teamId).orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found."));

        // TODO: FantasyTeam.owner (RegisteredUser) replaced by ownerUserId (UUID) — model now mirrors schema.sql
        if (!fantasyTeam.getOwner().getUserId().equals(registeredId)) {
            throw new AuthorisationException("You do not own this fantasy team.");
        }

        // TODO: FantasyTeam.isLocked removed (no column, no derivation) — this check never fires (DAO always hardcodes false). Real lock state is fantasyRound.status/roundLock. Model now mirrors schema.sql
        if (fantasyTeam.getIsLocked()) {
            throw new BusinessRuleException("This fantasy team is locked.");
        }
        List<UUID> selectedPlayerIds = new ArrayList<>();
        List<FantasyTeamPlayerSelectionResponseDTO> selectedResponsePlayers = new ArrayList<>();

        for (FantasyTeamPlayerSelectionRequestDTO requestPlayers : fantasyTeamDTO.getSelectedPlayers()) {
            Player player = playerDAO.getPlayerById(requestPlayers.getPlayerId()).orElseThrow(() -> new ResourceNotFoundException("Player not found."));
            selectedPlayerIds.add(player.getPlayerId());
            selectedResponsePlayers.add(FantasyTeamPlayerSelectionResponseDTO.builder()
                    .playerId(player.getPlayerId())
                    .playerName(player.getPlayerName())
                    // TODO: Player.position/club replaced by positionId/clubId (UUID FK) — model now mirrors schema.sql
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

        SquadValidationResultDTO validationResult = squadValidationService.validateSquad(selectedPlayerIds);
        if (!validationResult.isValid()) {
            String firstError = validationResult.getErrors().get(0).getMessage();
            throw new BusinessRuleException("Squad validation failed: " + firstError);
        }

        BigDecimal totalTeamValue = totalTeamValue(fantasyTeamDTO);
        BigDecimal remainingBudget = INITIAL_BUDGET.subtract(totalTeamValue);
        if (remainingBudget.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("You cannot afford this squad. Insufficient remaining budget.");
        }

        fantasyTeamPlayerDAO.replaceSquad(teamId, selectedPlayerIds);

        boolean budgetUpdated = fantasyTeamDAO.updateBudgetAndValue(teamId, totalTeamValue, remainingBudget);
        if (!budgetUpdated) {
            throw new DataAccessException("Unable to update fantasy team budget and value.",null);
        }

        fantasyTeam.setTotalTeamValue(totalTeamValue);
        fantasyTeam.setRemainingBudget(remainingBudget);

        return FantasyTeamResponseDTO.builder()
                .teamId(fantasyTeam.getTeamId())
                .teamName(fantasyTeam.getTeamName())
                // TODO: FantasyTeam.owner (RegisteredUser) replaced by ownerUserId (UUID) — model now mirrors schema.sql
                .managerId(fantasyTeam.getOwner().getUserId())
                .managerUsername(fantasyTeam.getOwner().getUsername())
                .totalTeamValue(fantasyTeam.getTotalTeamValue())
                .remainingBudget(fantasyTeam.getRemainingBudget())
                // TODO: FantasyTeam.weeklyPoints/isLocked removed (no column, no derivation) — model now mirrors schema.sql
                .weeklyPoints(fantasyTeam.getWeeklyPoints())
                .totalPoints(fantasyTeam.getTotalPoints())
                .valid(fantasyTeam.getIsValid())
                // TODO: FantasyTeam.isLocked removed (no column, no derivation) — model now mirrors schema.sql
                .locked(fantasyTeam.getIsLocked())
                .selectedPlayers(selectedResponsePlayers)
                .build();
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
