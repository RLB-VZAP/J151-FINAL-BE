package com.vzap.trytons.service.fantasyteam;

import com.vzap.trytons.dao.catalog.ClubDAO;
import com.vzap.trytons.dao.scoring.FantasyPointsDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamPlayerDAO;
import com.vzap.trytons.dao.leaderboard.LeaderboardDAO;
import com.vzap.trytons.dao.catalog.PlayerDAO;
import com.vzap.trytons.dao.catalog.PositionDAO;
import com.vzap.trytons.dao.auth.RegisteredUserDAO;
import com.vzap.trytons.dto.fantasyteam.FantasyTeamPlayerSelectionRequestDTO;
import com.vzap.trytons.dto.fantasyteam.FantasyTeamPlayerSelectionResponseDTO;
import com.vzap.trytons.dto.fantasyteam.FantasyTeamRequestDTO;
import com.vzap.trytons.dto.fantasyteam.FantasyTeamResponseDTO;
import com.vzap.trytons.dto.fantasyteam.SquadValidationResultDTO;
import com.vzap.trytons.dto.fantasyteam.ViewOpponentTeamDTO;
import com.vzap.trytons.dto.fantasyteam.ViewOwnTeamDTO;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.BadRequestException;
import com.vzap.trytons.model.catalog.Club;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.catalog.Position;
import com.vzap.trytons.model.auth.RegisteredUser;
import com.vzap.trytons.model.fantasyteam.TeamPlayerSelection;
import com.vzap.trytons.enums.SquadRole;
import com.vzap.trytons.util.fantasyteam.SquadRoleAssigner;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FantasyTeamServiceImpl implements FantasyTeamService {
    // Squad budget lives on SquadValidationService now — see the doc comment
    // there for the rationale. TransferServiceImpl needs the same figure, so
    // it moved to the shared validator instead of staying private here.

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Inject
    private FantasyTeamPlayerDAO fantasyTeamPlayerDAO;

    @Inject
    private RegisteredUserDAO registeredUserDAO;

    @Inject
    private PlayerDAO playerDAO;

    @Inject
    private ClubDAO clubDAO;

    @Inject
    private PositionDAO positionDAO;
    @Inject
    private LeaderboardDAO leaderboardDAO;

    @Inject
    private FantasyPointsDAO fantasyPointsDAO;

    @Inject
    private SquadValidationService squadValidationService;
    @Override
    public FantasyTeamResponseDTO createTeam(UUID registeredUserId, FantasyTeamRequestDTO request) {
        List<UUID> playerIds = new ArrayList<>();
        List<TeamPlayerSelection> selections = new ArrayList<>();
        List<FantasyTeamPlayerSelectionResponseDTO> selectedPlayers = new ArrayList<>();
        Map<UUID, String> positionNameByPlayer = new HashMap<>();

        BigDecimal totalTeamValue = BigDecimal.ZERO;
        int totalPoints = 0;

        for (FantasyTeamPlayerSelectionRequestDTO requestPlayers : request.getSelectedPlayers()){
            Player player = playerDAO.getPlayerById(requestPlayers.getPlayerId()).orElseThrow(() -> new ResourceNotFoundException("Player Not Found."));
            playerIds.add(player.getPlayerId());

            totalTeamValue = totalTeamValue.add(player.getValue());

            TeamPlayerSelection selection = TeamPlayerSelection.builder()
                    .selectionId(UUID.randomUUID())
                    .teamId(null)
                    .playerId(player.getPlayerId())
                    .selectedDate(LocalDateTime.now())
                    .isCaptain(false)
                    .isViceCaptain(false)
                    .squadRole(requestPlayers.getSquadRole())
                    .build();

                    selections.add(selection);
            Club club = clubDAO.findByClubId(player.getClubId()).orElseThrow(() -> new ResourceNotFoundException("Club Not Found."));
            Position position = positionDAO.findById(player.getPositionId()).orElseThrow(() -> new ResourceNotFoundException("Position Not Found."));
            positionNameByPlayer.put(player.getPlayerId(), position.getPositionName());



            int playerPoints = fantasyPointsDAO.getTotalFinalPointsForPlayer(player.getPlayerId());
            totalPoints += playerPoints;
            selectedPlayers.add(FantasyTeamPlayerSelectionResponseDTO.builder()
                    .playerId(player.getPlayerId())
                    .playerName(player.getPlayerName())
                    .positionId(player.getPositionId())
                    .positionName(position.getPositionName())
                    .clubId(player.getClubId())
                    .clubName(club.getClubName())
                    .value(player.getValue())
                    .isActive(player.isActive())
                    .totalFantasyPoints(playerPoints)
                    .currentForm(player.getCurrentForm())
                    .build());
        }

        SquadValidationResultDTO validationResult = squadValidationService.validateSquad(playerIds);

        if(!validationResult.isValid()){
            String firstError = validationResult.getErrors().get(0).getMessage();
            throw new BusinessRuleException("Squad validation failed: " + firstError);
        }

        normaliseSquadRoles(playerIds, positionNameByPlayer, selections);

        BigDecimal remainingBudget = squadValidationService.checkRemainingBudget(
                squadValidationService.getInitialBudget().subtract(totalTeamValue),
                "You cannot afford this squad.Isnufficient balance.");
        FantasyTeam fantasyTeam = mapRequestToFantasyTeam(request);
        fantasyTeam.setTeamId(UUID.randomUUID());
        fantasyTeam.setOwnerUserId(registeredUserId);
        // Insert the budget already debited by the squad cost. Storing the full
        // INITIAL_BUDGET here relied on the updateBudget call below to correct
        // the row, so any failure in between left the team looking unspent.
        fantasyTeam.setRemainingBudget(remainingBudget);
        fantasyTeam.setCreationDate(LocalDateTime.now());
        fantasyTeam.setIsValid(true);

        FantasyTeam savedTeam = fantasyTeamDAO.createTeam(fantasyTeam).orElseThrow(() -> new BusinessRuleException("Unable to create fantasy team."));

        for(TeamPlayerSelection selection : selections){
            selection.setTeamId(savedTeam.getTeamId());
        }

        fantasyTeamPlayerDAO.replaceSquad(savedTeam.getTeamId(), selections);
        boolean budgetUpdated = fantasyTeamDAO.updateBudget(savedTeam.getTeamId(),remainingBudget);
        if(!budgetUpdated){
            throw new DataAccessException("Unable to update fantasy team budget.",null);
        }

        RegisteredUser manager = registeredUserDAO.getRegisteredUserById(registeredUserId)
                .orElseThrow(()-> new ResourceNotFoundException("Registered user not found."));

        return FantasyTeamResponseDTO.builder()
                .teamId(fantasyTeam.getTeamId())
                .teamName(fantasyTeam.getTeamName())
                .managerId(manager.getUserId())
                .managerUsername(manager.getUsername())
                .totalTeamValue(totalTeamValue)
                .remainingBudget(remainingBudget)
                .totalPoints(totalPoints)
                .valid(fantasyTeam.getIsValid())
                .selectedPlayers(selectedPlayers)
                .build();
    }

    @Override
    public FantasyTeamResponseDTO getOwnTeam(UUID registeredUserId) {
        if (registeredUserId == null) {
            throw new BadRequestException("Current user ID is required.");
        }

        // Summary only — teamId, name and budget. Callers that need the squad
        // already have viewOwnTeam, and this is used to answer "do I have a team
        // yet, and which one", so loading every selection would be wasteful.
        return fantasyTeamDAO.getTeamByOwner(registeredUserId)
                .map(team -> FantasyTeamResponseDTO.builder()
                        .teamId(team.getTeamId())
                        .teamName(team.getTeamName())
                        .managerId(team.getOwnerUserId())
                        .remainingBudget(team.getRemainingBudget())
                        .valid(team.getIsValid())
                        .build())
                .orElse(null);
    }

    @Override
    public ViewOpponentTeamDTO viewOpponentTeam(UUID teamId) {
        FantasyTeam fantasyTeam = fantasyTeamDAO.findTeamById(teamId);
        if(fantasyTeam == null){
            throw new ResourceNotFoundException("Fantasy Team not found.");
        }

        List<FantasyTeamPlayerSelectionResponseDTO> playerResponses = new ArrayList<>();
        List<TeamPlayerSelection> squad = fantasyTeamPlayerDAO.getSquadByTeamId(teamId);

        int totalPoints = 0;

        for(TeamPlayerSelection selection : squad){
            Player player = playerDAO.getPlayerById(selection.getPlayerId()).orElseThrow(() -> new ResourceNotFoundException("Player Not Found."));
            int playerPoints = fantasyPointsDAO.getTotalFinalPointsForPlayer(player.getPlayerId());
            totalPoints += playerPoints;
            Club club = clubDAO.findByClubId(player.getClubId()).orElseThrow(() -> new ResourceNotFoundException("Club Not Found."));
            Position position = positionDAO.findById(player.getPositionId()).orElseThrow(() -> new ResourceNotFoundException("Position Not Found."));

            FantasyTeamPlayerSelectionResponseDTO response  = FantasyTeamPlayerSelectionResponseDTO.builder()
                    .playerId(player.getPlayerId())
                    .playerName(player.getPlayerName())
                    .positionId(player.getPositionId())
                    .positionName(position.getPositionName())
                    .clubId(player.getClubId())
                    .clubName(club.getClubName())
                    .value(player.getValue())
                    .isActive(player.isActive())
                    .attackingAbility(player.getAttackingAbility())
                    .defensiveAbility(player.getDefensiveAbility())
                    .kickingAbility(player.getKickingAbility())
                    .discipline(player.getDiscipline())
                    .consistency(player.getConsistency())
                    .fitness(player.getFitness())
                    .currentForm(player.getCurrentForm())
                    .totalFantasyPoints(playerPoints)
                    .squadRole(selection.getSquadRole())
                    .isCaptain(selection.getIsCaptain())
                    .isViceCaptain(selection.getIsViceCaptain())
                    .build();

            playerResponses.add(response);
        }

        return ViewOpponentTeamDTO.builder()
                .teamId(teamId)
                .teamName(fantasyTeam.getTeamName())
                .totalPoints(totalPoints)
                .weeklyPoints(0)
                .players(playerResponses)
                .build();
    }

    @Override
    public ViewOwnTeamDTO viewOwnTeam(UUID registeredUserId, UUID teamId) {
        FantasyTeam fantasyTeam = fantasyTeamDAO.findTeamById(teamId);
        if(fantasyTeam == null){
            throw new ResourceNotFoundException("Fantasy Team not found.");
        }
        if(!fantasyTeam.getOwnerUserId().equals(registeredUserId)){
            throw new BusinessRuleException("You are not the owner of this fantasy team.");
        }
        List<TeamPlayerSelection> squad = fantasyTeamPlayerDAO.getSquadByTeamId(teamId);
        List<FantasyTeamPlayerSelectionResponseDTO> playerResponsesDTO = new ArrayList<>();
        BigDecimal totalTeamValue = BigDecimal.ZERO;
        int totalPoints = 0;
        for(TeamPlayerSelection playerResponse : squad) {
            Player player = playerDAO.getPlayerById(playerResponse.getPlayerId()).orElseThrow(() -> new ResourceNotFoundException("Player Not Found."));
            totalTeamValue = totalTeamValue.add(player.getValue());
            int playerPoints = fantasyPointsDAO.getTotalFinalPointsForPlayer(player.getPlayerId());
            totalPoints += playerPoints;
            Club club = clubDAO.findByClubId(player.getClubId()).orElseThrow(() -> new ResourceNotFoundException("Club Not Found."));
            Position position = positionDAO.findById(player.getPositionId()).orElseThrow(() -> new ResourceNotFoundException("Position Not Found."));
            FantasyTeamPlayerSelectionResponseDTO response = FantasyTeamPlayerSelectionResponseDTO.builder()
                    .playerId(player.getPlayerId())
                    .playerName(player.getPlayerName())
                    .positionId(player.getPositionId())
                    .positionName(position.getPositionName())
                    .clubId(player.getClubId())
                    .clubName(club.getClubName())
                    .value(player.getValue())
                    .isActive(player.isActive())
                    .attackingAbility(player.getAttackingAbility())
                    .defensiveAbility(player.getDefensiveAbility())
                    .kickingAbility(player.getKickingAbility())
                    .discipline(player.getDiscipline())
                    .consistency(player.getConsistency())
                    .fitness(player.getFitness())
                    .currentForm(player.getCurrentForm())
                    .totalFantasyPoints(playerPoints)
                    .squadRole(playerResponse.getSquadRole())
                    .isCaptain(playerResponse.getIsCaptain())
                    .isViceCaptain(playerResponse.getIsViceCaptain())
                    .build();
            playerResponsesDTO.add(response);
        }

            RegisteredUser owner = registeredUserDAO.getRegisteredUserById(fantasyTeam.getOwnerUserId()).orElseThrow(() -> new ResourceNotFoundException("Owner Not Found."));

            return ViewOwnTeamDTO.builder()
                    .teamId(fantasyTeam.getTeamId())
                    .teamName(fantasyTeam.getTeamName())
                    .totalTeamValue(totalTeamValue)
                    .remainingBudget(fantasyTeam.getRemainingBudget())
                    .creationDate(fantasyTeam.getCreationDate())
                    .totalPoints(totalPoints)
                    .isValid(fantasyTeam.getIsValid())
                    .ownerUsername(owner.getUsername())
                    .players(playerResponsesDTO)
                    .build();
        }


    @Override
    public FantasyTeamResponseDTO updateTeam(UUID registeredId, UUID teamId, FantasyTeamRequestDTO fantasyTeamDTO) {
        FantasyTeam fantasyTeam = fantasyTeamDAO.getTeamById(teamId).orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found."));
        if(!fantasyTeam.getOwnerUserId().equals(registeredId)){
            throw new BusinessRuleException("You are not the owner of this fantasy team.");
        }

        List<UUID> selectedPlayerIds = new ArrayList<>();
        List<TeamPlayerSelection> selections = new ArrayList<>();
        List<FantasyTeamPlayerSelectionResponseDTO> selectedResponsePlayers = new ArrayList<>();
        Map<UUID, String> positionNameByPlayer = new HashMap<>();

        int totalPoints = 0;

        for (FantasyTeamPlayerSelectionRequestDTO requestPlayers : fantasyTeamDTO.getSelectedPlayers()) {
            Player player = playerDAO.getPlayerById(requestPlayers.getPlayerId()).orElseThrow(() -> new ResourceNotFoundException("Player not found."));
            Club club = clubDAO.findByClubId(player.getClubId()).orElseThrow(() -> new ResourceNotFoundException("Club Not Found."));
            Position position = positionDAO.findById(player.getPositionId()).orElseThrow(() -> new ResourceNotFoundException("Position Not Found."));
            positionNameByPlayer.put(player.getPlayerId(), position.getPositionName());
            selectedPlayerIds.add(player.getPlayerId());
            TeamPlayerSelection selection = TeamPlayerSelection.builder()
                    .selectionId(UUID.randomUUID())
                    .teamId(teamId)
                    .playerId(player.getPlayerId())
                    .selectedDate(LocalDateTime.now())
                    .isCaptain(false)
                    .isViceCaptain(false)
                    .squadRole(requestPlayers.getSquadRole())
                    .build();

            selections.add(selection);

            int playerPoints = fantasyPointsDAO.getTotalFinalPointsForPlayer(player.getPlayerId());
            totalPoints += playerPoints;
            selectedResponsePlayers.add(FantasyTeamPlayerSelectionResponseDTO.builder()
                    .playerId(player.getPlayerId())
                    .playerName(player.getPlayerName())
                    .positionId(player.getPositionId())
                    .positionName(position.getPositionName())
                    .clubId(player.getClubId())
                    .clubName(club.getClubName())
                    .value(player.getValue())
                    .isActive(player.isActive())
                    .totalFantasyPoints(playerPoints)
                    .currentForm(player.getCurrentForm())
                    .build());
        }

        SquadValidationResultDTO validationResult = squadValidationService.validateSquad(selectedPlayerIds);
        if (!validationResult.isValid()) {
            String firstError = validationResult.getErrors().get(0).getMessage();
            throw new BusinessRuleException("Squad validation failed: " + firstError);
        }

        normaliseSquadRoles(selectedPlayerIds, positionNameByPlayer, selections);

        BigDecimal totalTeamValue = totalTeamValue(fantasyTeamDTO);
        BigDecimal remainingBudget = squadValidationService.checkRemainingBudget(
                squadValidationService.getInitialBudget().subtract(totalTeamValue),
                "You cannot afford this squad. Insufficient remaining budget.");

        fantasyTeamPlayerDAO.replaceSquad(teamId, selections);

        boolean budgetUpdated = fantasyTeamDAO.updateBudget(teamId, remainingBudget);
        if (!budgetUpdated) {
            throw new DataAccessException("Unable to update fantasy team budget and value.",null);
        }


        fantasyTeam.setRemainingBudget(remainingBudget);

        RegisteredUser manager = registeredUserDAO.getRegisteredUserById(registeredId).orElseThrow(() -> new ResourceNotFoundException("Manager Not Found."));

        return FantasyTeamResponseDTO.builder()
                .teamId(fantasyTeam.getTeamId())
                .teamName(fantasyTeam.getTeamName())
                .managerId(manager.getUserId())
                .managerUsername(manager.getUsername())
                .totalTeamValue(totalTeamValue)
                .remainingBudget(remainingBudget)
                .totalPoints(totalPoints)
                .valid(fantasyTeam.getIsValid())
                .selectedPlayers(selectedResponsePlayers)
                .build();
    }

    /**
     * Repairs the STARTING/BENCH split so it always ends up 15/5 by position
     * quota, regardless of what the client sent. FantasyTeamServlet used to
     * hardcode every player to STARTING (see buildFantasyTeamRequest), so
     * every UI-created team had a 20/0 split and no bench at all. Runs after
     * squadValidationService.validateSquad has already confirmed the player
     * set is a legal 20-player squad with valid per-position min/max counts
     * — that guarantee is what makes SquadRoleAssigner's quotas always sum
     * to exactly 15 STARTING here.
     *
     * <p>If the incoming selections are already exactly 15/5, they are left
     * untouched so a manager's own bench choice within a legal split is
     * respected; only a broken split gets rewritten.
     */
    private void normaliseSquadRoles(List<UUID> playerIds, Map<UUID, String> positionNameByPlayer,
                                      List<TeamPlayerSelection> selections) {
        long startingCount = selections.stream().filter(s -> s.getSquadRole() == SquadRole.STARTING).count();
        long benchCount = selections.stream().filter(s -> s.getSquadRole() == SquadRole.BENCH).count();

        if (startingCount != SquadRoleAssigner.STARTING_SIZE || benchCount != SquadRoleAssigner.BENCH_SIZE) {
            Map<UUID, SquadRole> assignedRoles = SquadRoleAssigner.assignRoles(playerIds, positionNameByPlayer::get);
            for (TeamPlayerSelection selection : selections) {
                selection.setSquadRole(assignedRoles.get(selection.getPlayerId()));
            }
        }

        // Defence in depth: even after normalisation, confirm the split that
        // is about to be persisted is exactly 15/5 and fail loudly rather
        // than silently writing a broken squad if that invariant is ever
        // violated (e.g. by a future change to position quotas).
        SquadValidationResultDTO roleValidation = squadValidationService.validateSquadRoles(
                selections.stream().map(TeamPlayerSelection::getSquadRole).collect(Collectors.toList()));
        if (!roleValidation.isValid()) {
            String firstError = roleValidation.getErrors().get(0).getMessage();
            throw new BusinessRuleException("Squad validation failed: " + firstError);
        }
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
