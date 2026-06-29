package com.vzap.trytons.dao;
import com.vzap.trytons.model.Position;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionDAO {
    Optional<Position> findById(UUID positionId);
    Optional<Position> findByName(String positionName);
    Optional<Position> findByPositionCategory(String positionCategory);
    List<Position> findAllPositions();
    boolean createPosition(Position position);
    boolean updatePosition(Position position);
     boolean existsByName(String positionName);
}
