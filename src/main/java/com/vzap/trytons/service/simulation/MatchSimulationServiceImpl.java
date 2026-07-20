package com.vzap.trytons.service.simulation;

import com.vzap.trytons.dao.admin.AdminDAO;
import com.vzap.trytons.dao.catalog.PlayerDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamRoundSelectionDAO;
import com.vzap.trytons.dao.fixture.FantasyRoundDAO;
import com.vzap.trytons.dao.fixture.FixtureDAO;
import com.vzap.trytons.dao.results.MatchResultDAO;
import com.vzap.trytons.dao.scoring.ScoringRuleDAO;
import com.vzap.trytons.dto.results.MatchResultResponseDTO;
import com.vzap.trytons.dto.simulation.SimulationSettingResponseDTO;
import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.catalog.PlayerAvailability;
import com.vzap.trytons.model.fantasyteam.FantasyTeamRoundSelection;
import com.vzap.trytons.model.fixture.FantasyRound;
import com.vzap.trytons.model.fixture.Fixture;
import com.vzap.trytons.model.scoring.ScoringRule;
import com.vzap.trytons.service.results.MatchResultService;
import com.vzap.trytons.service.results.PlayerStatisticsService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class MatchSimulationServiceImpl implements MatchSimulationService {
    // TODO: Generate a simulated match result for the fixture from the locked squad snapshots, // using player abilities, form, fitness and availability plus the active // simulation settings, persist the result, and return it as a MatchResultResponseDTO.
    @Inject
    private FixtureDAO fixtureDAO;

    @Inject
    private FantasyRoundDAO fantasyRoundDAO;

    @Inject
    private FantasyTeamRoundSelectionDAO roundSelectionDAO;

    @Inject
    private PlayerDAO playerDAO;

    @Inject
    private SimulationSettingService simulationSettingService;

    @Inject
    private MatchResultDAO matchResultDAO;

    @Inject
    private MatchResultService matchResultService;

    @Inject
    private PlayerStatisticsService playerStatisticsService;

    @Inject
    private AdminDAO adminDAO;

    @Inject
    private ScoringRuleDAO scoringRuleDAO;

    private final static int TEAM_SIZE = 20;

    @Override
    public MatchResultResponseDTO simulateFixture(UUID actorUserId, UUID fixtureId) {

        if (actorUserId == null) {
            throw new BusinessRuleException("An administrator is required to simulate a fixture.");
        }

        if (fixtureId == null) {
            throw new BusinessRuleException("Fixture ID is required.");
        }

        Fixture fixture = fixtureDAO.findFixtureById(fixtureId).orElseThrow(() -> new ResourceNotFoundException("Fixture not found"));

        if (adminDAO.getAdminById(actorUserId).isEmpty()) {
            throw new AuthorisationException("Only administrators may simulate fixtures.");
        }

        if (fixture.getStatus() != FixtureStatus.LOCKED) {
            throw new BusinessRuleException("Only a locked fixture can be simulated.");
        }

        if (matchResultDAO.findCurrentByFixtureId(fixtureId).isPresent()) {
            throw new ConflictException("A current result already exists for this fixture.");
        }

        FantasyRound round = fantasyRoundDAO.getRoundById(fixture.getRoundId()).orElseThrow(() -> new ResourceNotFoundException("Round not found"));
        LocalDateTime now = LocalDateTime.now();

        if (round.getLockDeadline() == null) {
            throw new BusinessRuleException("The round does not have a lock deadline.");
        }

        if (now.isBefore(round.getLockDeadline())) {
            throw new BusinessRuleException("The round has not reached its lock deadline.");
        }

        if ((round.getStatus() != FantasyRoundStatus.LOCKED) && (round.getStatus() != FantasyRoundStatus.IN_PROGRESS)) {
            throw new BusinessRuleException("The fixture can only be simulated when its round is locked.");
        }

        List<FantasyTeamRoundSelection> teamASelections = roundSelectionDAO.getSelectionsByRoundIdAndTeamId(round.getRoundId(), fixture.getTeamAId());
        List<FantasyTeamRoundSelection> teamBSelections = roundSelectionDAO.getSelectionsByRoundIdAndTeamId(round.getRoundId(), fixture.getTeamBId());

        if (teamASelections.isEmpty()) {
            throw new BusinessRuleException("Team A does not have a locked squad for this round.");
        }

        if (teamBSelections.isEmpty()) {
            throw new BusinessRuleException("Team B does not have a locked squad for this round.");
        }

        List<Player> teamA = new ArrayList<>();
        List<Player> teamB = new ArrayList<>();

        Map<UUID, AvailabilityStatus> availabilityByPlayerId = new HashMap<>();

        for (FantasyTeamRoundSelection selectionA : teamASelections) {
            Player player = playerDAO.getPlayerById(selectionA.getPlayerId()).orElseThrow(() -> new ResourceNotFoundException("A locked player for Team A could not be found."));

            PlayerAvailability availability = playerDAO.getCurrentAvailability(player.getPlayerId()).orElseThrow(() -> new BusinessRuleException("Team A player does not have a current availability."));

            teamA.add(player);
            availabilityByPlayerId.put(player.getPlayerId(), availability.getStatus());
        }

        for (FantasyTeamRoundSelection selectionB : teamBSelections) {
            Player player = playerDAO.getPlayerById(selectionB.getPlayerId()).orElseThrow(() -> new ResourceNotFoundException("A locked player for Team A could not be found."));

            PlayerAvailability availability = playerDAO.getCurrentAvailability(player.getPlayerId()).orElseThrow(() -> new BusinessRuleException("Team A player does not have a current availability."));

            teamB.add(player);
            availabilityByPlayerId.put(player.getPlayerId(), availability.getStatus());
        }


        if (teamASelections.size() != TEAM_SIZE) {
            throw new BusinessRuleException("Team A does not have a complete 20-player locked squad.");
        }

        if (teamBSelections.size() != TEAM_SIZE) {
            throw new BusinessRuleException("Team B does not have a complete 20-player locked squad.");
        }

        SimulationSettingResponseDTO settings = simulationSettingService.getActiveSimulationSetting();

        if (!round.getSeason().equalsIgnoreCase(settings.getSeason())) {
            throw new BusinessRuleException("The active simulation settings do not match the round season.");
        }

        List<ScoringRule> scoringRules = scoringRuleDAO.findActiveRules(round.getSeason());

        if (scoringRules.isEmpty()) {
            throw new BusinessRuleException("No active scoring rules exist for this round.");
        }
        int simulationRunNumber = matchResultDAO.getNextSimulationRunNumber(fixtureId);

        long randomSeed = fixtureId.hashCode() + simulationRunNumber;
        Random random = new Random(randomSeed);

        Random random = new Random(randomSeed);
    }
}