package com.vzap.trytons.dao;

import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.FantasyRound;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.vzap.trytons.util.DBConnectionManager.getConnection;

public class FantasyRoundDAOImpl implements FantasyRoundDAO {
    private static final Logger LOG = Logger.getLogger(FantasyRound.class.getName());


    @Override
    public Optional<FantasyRound> getRoundById(UUID roundId) {

        String query = "SELECT * FROM fantasyRound WHERE roundId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, roundId.toString());

            try(ResultSet rs = ps.executeQuery()){

                if (rs.next()){

                    FantasyRound fr = FantasyRound.builder()
                            .roundId(roundId)
                            .season(rs.getString("season"))
                            .roundNumber(rs.getInt("roundNumber"))
                            .openDate(rs.getObject("openDate", LocalDateTime.class))
                            .lockDeadline(rs.getObject("lockDeadline", LocalDateTime.class))
                            .endDate(rs.getObject("endDate", LocalDateTime.class))
                            .status(FantasyRoundStatus.valueOf(rs.getString("status")))

                            .build();

                    return Optional.of(fr);
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not find fantasy round by ID", e);
            throw new DataAccessException("Could not find fantasy round by ID", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<FantasyRound> getRoundBySeasonAndNumber(String season, int roundNumber) {
        return Optional.empty();
    }

    @Override
    public List<FantasyRound> getAllRounds() {
        return List.of();
    }

    @Override
    public List<FantasyRound> getRoundsByStatus(FantasyRoundStatus status) {
        return List.of();
    }

    @Override
    public Optional<FantasyRound> getCurrentOpenRound() {
        return Optional.empty();
    }

    @Override
    public boolean updateRoundStatus(UUID roundId, FantasyRoundStatus status) {
        return false;
    }

    @Override
    public boolean roundExists(UUID roundId) {
        return false;
    }
}
