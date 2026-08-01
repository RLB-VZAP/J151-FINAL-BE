package com.vzap.trytons.dao.results;

import com.vzap.trytons.model.results.PlayerStatistics;
import com.vzap.trytons.model.results.PointsByEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerStatisticsDAO {
    List<PlayerStatistics> findByResultId(UUID resultId);
    List<PlayerStatistics> findByResultIdAndTeamId(UUID resultId, UUID teamId);
    Optional<PlayerStatistics> findByResultIdAndTeamIdAndPlayerId(UUID resultId, UUID teamId, UUID playerId);
    Optional<PlayerStatistics> save(PlayerStatistics playerStatistics);
    Optional<PlayerStatistics> findById(UUID statId);

    /**
     * How one team's fantasy points for a match result were earned, grouped by
     * the kind of event that produced them.
     *
     * <p>Aggregated across every player in the team, so a fixture can show
     * that a total of 190 came from tries, tackles, assists and so on, less
     * whatever the deductions cost.
     */
    List<PointsByEvent> findPointsByEventForResultAndTeam(UUID resultId, UUID teamId);
}
