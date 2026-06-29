package com.vzap.trytons.dao;

import com.vzap.trytons.model.Position;

import java.util.Optional;
import java.util.UUID;

public interface PositionDAO {
    Optional<Position> findById(UUID PositionId);
    Optional<Position> findByName(String positionName);
    Optional<Position> findByPositionCategory(String positionCategory);
    Optional<Position> findAllPositions();
    public boolean createPosition(Position position);
    public boolean updatePosition(Position position);
    public boolean deletePosition(Position position);
    public boolean existsByName(String positionName);
}
