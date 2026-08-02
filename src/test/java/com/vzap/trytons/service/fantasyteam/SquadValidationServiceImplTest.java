package com.vzap.trytons.service.fantasyteam;

import com.vzap.trytons.dao.catalog.PlayerDAO;
import com.vzap.trytons.dto.fantasyteam.SquadValidationResultDTO;
import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.enums.SquadRole;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.catalog.PlayerAvailability;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SquadValidationService is now the single home for squad-value/budget
 * validation shared by FantasyTeamServiceImpl (create/update team) and
 * TransferServiceImpl (executeTransfer) - see the DEFECT 1 fix. These tests
 * cover that centralised budget logic: live price summation, and the
 * under/over-budget decision plus its remaining-budget arithmetic.
 *
 * No Mockito dependency is declared in pom.xml, so PlayerDAO is hand-faked
 * here (mirroring the interface's contract rather than a mocking framework).
 */
class SquadValidationServiceImplTest {

    /** Minimal in-memory stand-in for PlayerDAO; only getPlayerById is exercised by these tests. */
    private static class FakePlayerDAO implements PlayerDAO {
        private final Map<UUID, Player> players = new HashMap<>();

        void add(Player player) {
            players.put(player.getPlayerId(), player);
        }

        @Override
        public Optional<Player> getPlayerById(UUID playerId) {
            return Optional.ofNullable(players.get(playerId));
        }

        @Override
        public List<Player> getAllPlayers() {
            return new ArrayList<>(players.values());
        }

        @Override
        public Map<UUID, AvailabilityStatus> getCurrentAvailabilityStatuses(Collection<UUID> playerIds) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public List<Player> searchPlayers(String playerName, UUID clubId, UUID positionId, BigDecimal minValue,
                                           BigDecimal maxValue, Integer minCurrentForm, Integer maxCurrentForm,
                                           AvailabilityStatus availabilityStatus, Boolean isActive) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public Optional<Player> createPlayer(Player player) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public Optional<Player> updatePlayer(Player player) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public boolean updateValue(UUID playerId, BigDecimal newValue) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public boolean deactivatePlayer(UUID playerId) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public List<Player> getPlayersByClubId(UUID clubId) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public List<Player> getPlayersByPositionId(UUID positionId) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public Optional<PlayerAvailability> getCurrentAvailability(UUID playerId) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public List<PlayerAvailability> getAvailabilityHistory(UUID playerId) {
            throw new UnsupportedOperationException("not needed for this test");
        }
    }

    private FakePlayerDAO playerDAO;
    private SquadValidationServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        playerDAO = new FakePlayerDAO();
        service = new SquadValidationServiceImpl();
        // No CDI container in a plain unit test, so wire the @Inject field by hand.
        Field field = SquadValidationServiceImpl.class.getDeclaredField("playerDAO");
        field.setAccessible(true);
        field.set(service, playerDAO);
    }

    private Player player(BigDecimal value) {
        Player player = Player.builder()
                .playerId(UUID.randomUUID())
                .playerName("Player " + UUID.randomUUID())
                .value(value)
                .build();
        playerDAO.add(player);
        return player;
    }

    @Test
    void computeSquadValueSumsLivePlayerValues() {
        Player a = player(new BigDecimal("10.50"));
        Player b = player(new BigDecimal("20.25"));

        BigDecimal total = service.computeSquadValue(List.of(a.getPlayerId(), b.getPlayerId()));

        assertEquals(new BigDecimal("30.75"), total);
    }

    @Test
    void computeSquadValueThrowsWhenPlayerDoesNotExist() {
        assertThrows(ResourceNotFoundException.class,
                () -> service.computeSquadValue(List.of(UUID.randomUUID())));
    }

    @Test
    void getInitialBudgetIsTheSharedSquadBudget() {
        assertEquals(new BigDecimal("196.00"), service.getInitialBudget());
    }

    @Test
    void underBudgetSquadPassesAndReturnsRemainingBudget() {
        BigDecimal totalSquadValue = new BigDecimal("180.00");

        BigDecimal remaining = service.checkRemainingBudget(
                service.getInitialBudget().subtract(totalSquadValue),
                "You cannot afford this squad.");

        assertEquals(new BigDecimal("16.00"), remaining);
    }

    @Test
    void overBudgetSquadIsRejectedWithTheGivenMessage() {
        BigDecimal totalSquadValue = new BigDecimal("200.00");
        BigDecimal wouldBeRemaining = service.getInitialBudget().subtract(totalSquadValue);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> service.checkRemainingBudget(wouldBeRemaining, "You cannot afford this squad."));

        assertEquals("You cannot afford this squad.", exception.getMessage());
    }

    @Test
    void remainingBudgetArithmeticMatchesBudgetMinusTotalValue() {
        Player a = player(new BigDecimal("50.00"));
        Player b = player(new BigDecimal("75.50"));

        BigDecimal totalSquadValue = service.computeSquadValue(List.of(a.getPlayerId(), b.getPlayerId()));
        BigDecimal remaining = service.checkRemainingBudget(
                service.getInitialBudget().subtract(totalSquadValue),
                "You cannot afford this squad.");

        assertEquals(new BigDecimal("70.50"), remaining);
    }

    @Test
    void exactlyOnBudgetIsAllowedNotRejected() {
        BigDecimal remaining = service.checkRemainingBudget(BigDecimal.ZERO, "You cannot afford this squad.");

        assertTrue(remaining.compareTo(BigDecimal.ZERO) == 0);
    }

    /**
     * Covers the bench bug's other half: SquadValidationServiceImpl only
     * checked squad size (== 20), never the STARTING/BENCH split, so a
     * 20-STARTING/0-BENCH squad (every UI-created team, before the
     * FantasyTeamServlet fix) reported "Valid squad". These tests exercise
     * validateSquadRoles directly, which needs no DAO/DB.
     */
    @Test
    void fifteenStartingFiveBenchIsValid() {
        List<SquadRole> roles = new ArrayList<>();
        for (int i = 0; i < 15; i++) roles.add(SquadRole.STARTING);
        for (int i = 0; i < 5; i++) roles.add(SquadRole.BENCH);

        SquadValidationResultDTO result = service.validateSquadRoles(roles);

        assertTrue(result.isValid());
    }

    @Test
    void twentyStartingZeroBenchIsRejected() {
        // Exactly the shape Jarryd XV and Test 1 were found in: every player
        // hardcoded STARTING because FantasyTeamServlet never set a bench.
        List<SquadRole> roles = new ArrayList<>();
        for (int i = 0; i < 20; i++) roles.add(SquadRole.STARTING);

        SquadValidationResultDTO result = service.validateSquadRoles(roles);

        assertTrue(!result.isValid());
        assertEquals("INVALID_SQUAD_ROLE_SPLIT", result.getErrors().get(0).getCode());
    }

    @Test
    void seededFourteenSixSplitIsAlsoRejected() {
        List<SquadRole> roles = new ArrayList<>();
        for (int i = 0; i < 14; i++) roles.add(SquadRole.STARTING);
        for (int i = 0; i < 6; i++) roles.add(SquadRole.BENCH);

        SquadValidationResultDTO result = service.validateSquadRoles(roles);

        assertTrue(!result.isValid());
    }
}
