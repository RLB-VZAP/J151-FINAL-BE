package com.vzap.trytons.roster.dao;

import com.vzap.trytons.roster.model.FantasyTeamRoundSelection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FantasyTeamRoundSelectionDAOImpl implements FantasyTeamRoundSelectionDAO {
    @Override
    public Optional<FantasyTeamRoundSelection> createRoundSelection(FantasyTeamRoundSelection selection) {
        return Optional.empty();
    }

    @Override
    public int createRoundSelections(List<FantasyTeamRoundSelection> selections) {
        return 0;
    }

    @Override
    public Optional<FantasyTeamRoundSelection> getRoundSelectionById(UUID selectionId) {
        return Optional.empty();
    }

    @Override
    public List<FantasyTeamRoundSelection> getSelectionsByRoundId(UUID roundId) {
        return List.of();
    }

    @Override
    public List<FantasyTeamRoundSelection> getSelectionsByRoundIdAndTeamId(UUID roundId, UUID teamId) {
        return List.of();
    }

    @Override
    public List<UUID> getTeamIdsWithSnapshotsForRound(UUID roundId) {
        return List.of();
    }

    @Override
    public boolean snapshotsExistForRound(UUID roundId) {
        return false;
    }

    @Override
    public boolean snapshotExistsForTeamInRound(UUID roundId, UUID teamId) {
        return false;
    }

    @Override
    public int countSelectionsByRoundId(UUID roundId) {
        return 0;
    }

    @Override
    public int countSelectionsByRoundIdAndTeamId(UUID roundId, UUID teamId) {
        return 0;
    }
}
