package com.vzap.trytons.service;

import com.vzap.trytons.dao.FantasyTeamDAO;
import com.vzap.trytons.dao.FantasyTeamPlayerDAO;
import com.vzap.trytons.dao.PlayerDAO;
import com.vzap.trytons.dto.RecommendedPlayerDTO;
import com.vzap.trytons.dto.TransferRecommendationRequestDTO;
import com.vzap.trytons.dto.TransferRecommendationResponseDTO;
import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Player;
import com.vzap.trytons.model.TeamPlayerSelection;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class TransferRecommendationServiceImpl implements  TransferRecommendationService {

    private static final int CANDIDATES_PER_PLAYER = 3;
    private static final int MAX_TOTAL_RECOMMENDATIONS = 10;

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;
    @Inject
    private FantasyTeamPlayerDAO fantasyTeamPlayerDAO;
    @Inject
    private PlayerDAO playerDAO;

    @Override
    public TransferRecommendationResponseDTO recommendTransfers(UUID actorUserId, TransferRecommendationRequestDTO request) {

        FantasyTeam team = fantasyTeamDAO.getTeamById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found"));

        // TODO: FantasyTeam.owner (RegisteredUser) replaced by ownerUserId (UUID) — model now mirrors schema.sql
        if(!team.getOwner().getUserId().equals(actorUserId)) {
            throw new AuthenticationException("You do not own this fantasy team");
        }

        List<TeamPlayerSelection> currentSquad = fantasyTeamPlayerDAO.getSquadByTeamId(request.getTeamId());

        if(currentSquad.isEmpty()) {
            return TransferRecommendationResponseDTO.builder()
                    .teamId(team.getTeamId())
                    .recommendations(List.of())
                    .build();
        }

        // TODO: TeamPlayerSelection.player replaced by playerId (UUID FK); cascades to a type-inference error below — model now mirrors schema.sql
        Set<UUID> squadPlayerIds = currentSquad.stream()
                .map(s -> s.getPlayer().getPlayerId())
                .collect(Collectors.toSet());

        List<Player> focusPlayers;
        if(request.getCurrentPlayerId() != null) {
            // TODO: TeamPlayerSelection.player replaced by playerId (UUID FK) — model now mirrors schema.sql
            Player focusPlayer = currentSquad.stream()
                    .map(TeamPlayerSelection::getPlayer)
                    .filter(p -> p.getPlayerId().equals(request.getCurrentPlayerId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Player not found in the squad"));

            focusPlayers = List.of(focusPlayer);
        }else{
            // TODO: TeamPlayerSelection.player replaced by playerId (UUID FK) — model now mirrors schema.sql
            focusPlayers = currentSquad.stream()
                    .map(TeamPlayerSelection::getPlayer)
                    .collect(Collectors.toList());
        }

        List<RecommendedPlayerDTO> recommendations = new ArrayList<>();

        for(Player outgoing : focusPlayers){
            recommendations.addAll(findReplacementsFor(outgoing, team, squadPlayerIds));
        }

        List<RecommendedPlayerDTO> finalRecommendations = recommendations.stream()
                .sorted(Comparator.comparingInt(RecommendedPlayerDTO::getCurrentForm).reversed())
                .limit(MAX_TOTAL_RECOMMENDATIONS)
                .collect(Collectors.toList());

        return TransferRecommendationResponseDTO.builder()
                .teamId(team.getTeamId())
                .recommendations(finalRecommendations)
                .build();
    }

    private List<RecommendedPlayerDTO> findReplacementsFor(Player outgoing, FantasyTeam team, Set<UUID> squadPlayerIds){

        BigDecimal affordableBudget = team.getRemainingBudget().add(outgoing.getValue());

        // TODO: Player.position replaced by positionId (UUID FK) — model now mirrors schema.sql
        List<Player> candidates = playerDAO.searchPlayers(
                null,
                null,
                outgoing.getPosition().getPositionId(),
                null,
                affordableBudget,
                null,
                null,
                null,
                null,
                AvailabilityStatus.ACTIVE,
                true
        );

        return candidates.stream()
                .filter(c -> !c.getPlayerId().equals(outgoing.getPlayerId()))
                .filter(c -> !squadPlayerIds.contains(c.getPlayerId()))
                .sorted(
                        Comparator.comparingInt(Player::getCurrentForm).reversed()
                                .thenComparing(Player::getValue))
                .limit(CANDIDATES_PER_PLAYER)
                .map(c -> toRecommendedPlayerDTO(c, outgoing))
                .collect(Collectors.toList());
    }

    private RecommendedPlayerDTO toRecommendedPlayerDTO(Player candidate, Player outgoing){
        return RecommendedPlayerDTO.builder()
                .playerId(candidate.getPlayerId())
                .playerName(candidate.getPlayerName())
                // TODO: Player.position/club replaced by positionId/clubId (UUID FK) — model now mirrors schema.sql
                .positionName(candidate.getPosition().getPositionName())
                .clubName(candidate.getClub().getClubName())
                .value(candidate.getValue())
                .currentForm(candidate.getCurrentForm())
                .availabilityStatus(AvailabilityStatus.ACTIVE.name())
                .replacesPlayerId(outgoing.getPlayerId())
                .reason(buildReason(candidate, outgoing))
                .build();
    }

    private String buildReason(Player candidate, Player outgoing) {
        if(candidate.getCurrentForm() > outgoing.getCurrentForm()){
            return String.format(
                    "Better current form (%d vs %d) in the same position",
                    candidate.getCurrentForm(), outgoing.getCurrentForm());
        }
        if(candidate.getValue().compareTo(outgoing.getValue()) < 0){
            return "Compare ability at a lower price, freeing up budget";
        }
        return "Available same-position option within your budget";
    }
}
