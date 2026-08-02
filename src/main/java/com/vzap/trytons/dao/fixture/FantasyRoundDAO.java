package com.vzap.trytons.dao.fixture;

import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.model.fixture.FantasyRound;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FantasyRoundDAO {
    Optional<FantasyRound> getRoundById(UUID roundId);
    Optional<FantasyRound> getRoundBySeasonAndNumber(String season, int roundNumber);
    List<FantasyRound> getAllRounds();
    List<FantasyRound> getRoundsByStatus(FantasyRoundStatus status);
    Optional<FantasyRound> getCurrentOpenRound();
    boolean updateRoundStatus(UUID roundId, FantasyRoundStatus status);

    /**
     * Moves a round's whole window at once.
     *
     * <p>All three timestamps are written together because
     * {@code chk_fantasyRound_dates} spans them -- writing them one at a time
     * would reject a perfectly valid move whenever the intermediate state
     * crossed the check. Only an UPCOMING round may be moved, so a round that
     * has already opened, locked or been played is left alone by the update
     * itself rather than only by a service-layer guard.
     *
     * @return true when a row was moved
     */
    boolean updateRoundSchedule(UUID roundId,
                                LocalDateTime openDate,
                                LocalDateTime lockDeadline,
                                LocalDateTime endDate);

    /**
     * The administrator "play this round now" override: drags a round's whole
     * window back so the next due-work pass picks it up.
     *
     * <p>Deliberately separate from {@link #updateRoundSchedule}, which only
     * ever moves an UPCOMING round. This one has to reach an OPEN round as well
     * -- a round whose transfer window is already open is exactly the one an
     * administrator wants to play early -- so it carries its own, wider status
     * guard. Keeping them apart means the manager-facing edit can never
     * accidentally acquire the admin override's reach.
     *
     * <p>All three timestamps still travel together, for the same
     * {@code chk_fantasyRound_dates} reason.
     *
     * @return true when a row was moved
     */
    boolean forceRoundSchedule(UUID roundId,
                               LocalDateTime openDate,
                               LocalDateTime lockDeadline,
                               LocalDateTime endDate);

    boolean roundExists(UUID roundId);

    FantasyRound createRound(FantasyRound round);
    /** Highest roundNumber used in a season, or 0 when the season has no rounds yet. */
    int getMaxRoundNumber(String season);
}