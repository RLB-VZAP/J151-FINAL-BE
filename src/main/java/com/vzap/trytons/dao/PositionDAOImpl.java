package com.vzap.trytons.dao;

import com.vzap.trytons.model.Position;

import java.util.Optional;
import java.util.UUID;

public class PositionDAOImpl extends BaseDAO implements PositionDAO {
    @Override
    public Optional<Position> findById(UUID PositionId) {
        return Optional.empty();
    }

    @Override
    public Optional<Position> findByName(String positionName) {
        return Optional.empty();
    }

    @Override
    public Optional<Position> findByPositionCategory(String positionCategory) {
        return Optional.empty();
    }

    @Override
    public Optional<Position> findAllPositions() {
        return Optional.empty();
    }

    @Override
    public boolean createPosition(Position position) {
        return false;
    }

    @Override
    public boolean updatePosition(Position position) {
        return false;
    }

    @Override
    public boolean deletePosition(Position position) {
        return false;
    }

    @Override
    public boolean existsByName(String positionName) {
        return false;
    }
}
