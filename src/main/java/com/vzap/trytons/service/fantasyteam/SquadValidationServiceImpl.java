package com.vzap.trytons.service.fantasyteam;

import com.vzap.trytons.dao.catalog.PlayerDAO;
import com.vzap.trytons.dao.catalog.PositionDAO;
import com.vzap.trytons.dto.fantasyteam.SquadValidationResultDTO;
import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.catalog.PlayerAvailability;
import com.vzap.trytons.model.catalog.Position;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.*;


@ApplicationScoped
public class SquadValidationServiceImpl implements SquadValidationService {
    /**
     * Squad budget, on the same scale as player.value — millions of rands, so
     * 196.00 means R196m. It has to match that scale because the budget is
     * spent by subtracting player values from it; the previous 100000000.00
     * was whole rands, which made every squad look free and overflowed
     * fantasyTeam.remainingBudget DECIMAL(10,2) on insert.
     *
     * Sized against the seeded roster and the 20-player squad rule. Once the
     * position minimums are applied, the cheapest legal squad costs about 185,
     * an average one about 203, and the most expensive about 222.
     *
     * 196 sits deliberately below the average squad, so a manager cannot just
     * take twenty players without thinking, while still leaving roughly 11
     * above the floor for a few premium picks. Anything near 190 would pin the
     * squad to the cheapest legal combination — the floor is high because the
     * roster is only 33 players for 20 places.
     *
     * Lives here (rather than in FantasyTeamServiceImpl, where it originated)
     * because TransferServiceImpl needs the same figure and this is the
     * shared squad/budget validator both flows already depend on.
     */
    private static final BigDecimal INITIAL_BUDGET = new BigDecimal("196.00");

    // Minimum position requirements.
    private static final int MIN_PROPS = 2;
    private static final int MIN_HOOKERS = 1;
    private static final int MIN_LOCKS = 2;
    private static final int MIN_LOOSE_FORWARDS = 3;
    private static final int MIN_SCRUM_HALVES = 1;
    private static final int MIN_FLY_HALVES = 1;
    private static final int MIN_CENTRES = 2;
    private static final int MIN_WINGS = 2;
    private static final int MIN_FULLBACKS = 1;

    // Maximum position requirements.
    private static final int MAX_PROPS = 4;
    private static final int MAX_HOOKERS = 2;
    private static final int MAX_LOCKS = 4;
    private static final int MAX_LOOSE_FORWARDS = 5;
    private static final int MAX_SCRUM_HALVES = 2;
    private static final int MAX_FLY_HALVES = 2;
    private static final int MAX_CENTRES = 4;
    private static final int MAX_WINGS = 4;
    private static final int MAX_FULLBACKS = 2;

    @Inject
    private PlayerDAO playerDAO;
    @Inject
    private PositionDAO positionDAO;

    @Override
    public SquadValidationResultDTO validateSquad(List<UUID> proposedPlayerIds) {
        return validateSquad(proposedPlayerIds, proposedPlayerIds);
    }

    @Override
    public SquadValidationResultDTO validateSquad(List<UUID> proposedPlayerIds, List<UUID> playersRequiringAvailabilityCheck) {
        SquadValidationResultDTO result = new SquadValidationResultDTO();
        validateDuplicatePlayers(proposedPlayerIds, result);
        List<Player> players = getPlayers(proposedPlayerIds, result);
        validateSquadSize(proposedPlayerIds, result);

        List<Player> playersToCheck = players.stream()
                .filter(p -> playersRequiringAvailabilityCheck.contains(p.getPlayerId()))
                .collect(java.util.stream.Collectors.toList());
        validatePlayerAvailability(playersToCheck, result);

        validatePositionRules(players, result);
        return result;
    }

    @Override
    public BigDecimal getInitialBudget() {
        return INITIAL_BUDGET;
    }

    @Override
    public BigDecimal computeSquadValue(List<UUID> playerIds) {
        BigDecimal total = BigDecimal.ZERO;
        for (UUID playerId : playerIds) {
            Player player = playerDAO.getPlayerById(playerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Player not found."));
            if (player.getValue() != null) {
                total = total.add(player.getValue());
            }
        }
        return total;
    }

    @Override
    public BigDecimal checkRemainingBudget(BigDecimal remainingBudget, String insufficientBudgetMessage) {
        if (remainingBudget.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException(insufficientBudgetMessage);
        }
        return remainingBudget;
    }

    private List<Player> getPlayers(List<UUID> playerIds, SquadValidationResultDTO result) {
        List<Player> players = new ArrayList<>();
        for (UUID playerId : playerIds) {
            Player player = validatePlayerIdsExist(playerId, result);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    private Player validatePlayerIdsExist(UUID playerId, SquadValidationResultDTO result) {
        Optional<Player> player = playerDAO.getPlayerById(playerId);
        if (player.isEmpty()) {
            result.addError("PLAYER_NOT_FOUND", "Player not found.", "List<UUID> proposedPlayersIds");
            return null;
        }
        return player.get();
    }

    private void validateSquadSize(List<UUID> players, SquadValidationResultDTO result) {
        int size = players.size();
        if (size != 20) {
            result.addError("INVALID_SQUAD_SIZE", "Squad size must be 20", "List<Player> players");
        }
    }

    private void validateDuplicatePlayers(List<UUID> proposedPlayerIds, SquadValidationResultDTO result) {
        Set<UUID> uniquePlayers = new HashSet<>();
        for (UUID playerId : proposedPlayerIds) {
            if (!uniquePlayers.add(playerId)) {
                result.addError("DUPLICATE_PLAYERS", "Duplicate player found.", "List<UUID> proposedPlayerIds");
                return;
            }
        }
    }


    private void validatePlayerAvailability(List<Player> players, SquadValidationResultDTO result) {

        for (Player player : players) {
            // No availability record means nothing has happened to this player:
            // records are written when someone is injured or suspended, so their
            // absence is the normal state of a fit player. Treating a missing record
            // as an error rejected every player who had never been unavailable.
            AvailabilityStatus status = playerDAO.getCurrentAvailability(player.getPlayerId())
                    .map(PlayerAvailability::getStatus)
                    .orElse(AvailabilityStatus.ACTIVE);
            if (status != AvailabilityStatus.ACTIVE) {
                result.addError("PLAYER_NOT_AVAILABLE", "Player is not available: " + player.getPlayerName(), "List<UUID> proposedPlayerIds");
            }
        }
    }


    private void validatePositionRules(List<Player> players, SquadValidationResultDTO result) {
        int propCount = 0;
        int hookerCount = 0;
        int lockCount = 0;
        int looseForwardCount = 0;
        int scrumHalfCount = 0;
        int flyHalfCount = 0;
        int centreCount = 0;
        int wingCount = 0;
        int fullbackCount = 0;
        int invalidCount = 0;

        for (Player player : players) {
            Position position = positionDAO.findById(player.getPositionId()).orElseThrow(() -> new ResourceNotFoundException("Position Not Found."));
            if (position.getPositionName() == null) {
                invalidCount++;
                continue;
            }

            switch (position.getPositionName()) {
                case "Prop":
                    propCount++;
                    break;

                case "Hooker":
                    hookerCount++;
                    break;

                case "Lock":
                    lockCount++;
                    break;

                case "Loose Forward":
                case "Flanker":
                case "Number Eight":
                    looseForwardCount++;
                    break;

                case "Scrum Half":
                    scrumHalfCount++;
                    break;

                case "Fly Half":
                    flyHalfCount++;
                    break;

                case "Centre":
                    centreCount++;
                    break;

                case "Wing":
                    wingCount++;
                    break;

                case "Fullback":
                    fullbackCount++;
                    break;

                default:
                    invalidCount++;
                    break;
            }
        }

        if (propCount < MIN_PROPS) {
            result.addError("INVALID_POSITION_COUNT", "Not enough props for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (hookerCount < MIN_HOOKERS) {
            result.addError("INVALID_POSITION_COUNT", "Not enough hookers for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (lockCount < MIN_LOCKS) {
            result.addError("INVALID_POSITION_COUNT", "Not enough locks for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (looseForwardCount < MIN_LOOSE_FORWARDS) {
            result.addError("INVALID_POSITION_COUNT", "Not enough loose forwards for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (scrumHalfCount < MIN_SCRUM_HALVES) {
            result.addError("INVALID_POSITION_COUNT", "Not enough scrum-halves for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (flyHalfCount < MIN_FLY_HALVES) {
            result.addError("INVALID_POSITION_COUNT", "Not enough fly-halves for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (centreCount < MIN_CENTRES) {
            result.addError("INVALID_POSITION_COUNT", "Not enough centres for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (wingCount < MIN_WINGS) {
            result.addError("INVALID_POSITION_COUNT", "Not enough wings for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (fullbackCount < MIN_FULLBACKS) {
            result.addError("INVALID_POSITION_COUNT", "Not enough fullbacks for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (propCount > MAX_PROPS) {
            result.addError("INVALID_POSITION_COUNT", "Too many props for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (hookerCount > MAX_HOOKERS) {
            result.addError("INVALID_POSITION_COUNT", "Too many hookers for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (lockCount > MAX_LOCKS) {
            result.addError("INVALID_POSITION_COUNT", "Too many locks for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (looseForwardCount > MAX_LOOSE_FORWARDS) {
            result.addError("INVALID_POSITION_COUNT", "Too many loose forwards for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (scrumHalfCount > MAX_SCRUM_HALVES) {
            result.addError("INVALID_POSITION_COUNT", "Too many scrum-halves for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (flyHalfCount > MAX_FLY_HALVES) {
            result.addError("INVALID_POSITION_COUNT", "Too many fly-halves for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (centreCount > MAX_CENTRES) {
            result.addError("INVALID_POSITION_COUNT", "Too many centres for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (wingCount > MAX_WINGS) {
            result.addError("INVALID_POSITION_COUNT", "Too many wings for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (fullbackCount > MAX_FULLBACKS) {
            result.addError("INVALID_POSITION_COUNT", "Too many fullbacks for eligible team", "List<UUID> proposedPlayerIds");
        }

        if (invalidCount > 0) {
            result.addError("INVALID_PLAYER_POSITION", invalidCount + " player(s) have an invalid or missing position", "List<UUID> proposedPlayerIds");
        }
    }
}
