package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FantasyPointsDAOImpl extends BaseDAO implements FantasyPointsDAO {
    private static final Logger LOG = Logger.getLogger(FantasyPointsDAO.class.getName());

    @Override
    public FantasyPoints save(FantasyPoints points) {

        UUID newId = UUID.randomUUID();
        String query = "INSERT INTO fantasyPoints "
                + "(pointsId, teamId, playerId, fixtureId, ruleId, pointsEarned, calculationDate, match_round_number, calculationVersion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, newId.toString());
            ps.setString(2, points.getFantasyTeam().getTeamId().toString());
            ps.setString(3, points.getPlayer().getPlayerId().toString());
            ps.setString(4, points.getFixture().getFixtureId().toString());
            if (points.getScoringRule() != null){
                ps.setString(5, points.getScoringRule().getRuleId().toString());

            } else {
                ps.setNull(5, Types.VARCHAR);
            }
            ps.setInt(6, points.getPointsEarned());
            ps.setTimestamp(7, Timestamp.valueOf(points.getCalculationDate()));
            ps.setInt(8, points.getMatchRoundNumber());
            ps.setInt(9, points.getCalculationVersion());

            points.setPointsId(newId);

            return points;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not save fantasy points", e);
            throw new DataAccessException("Could not save fantasy points", e);
        }
    }

    @Override
    public List<FantasyPoints> findByFixtureId(UUID fixtureId) {
            String query = "SELECT * FROM fantasyPoints WHERE fixtureId = ?";
            List<FantasyPoints> fantasyPoints = new ArrayList<>();
            try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, fixtureId.toString());

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        //creating shallow references to fulfil needed objects
                        UUID teamId = UUID.fromString(rs.getString("teamId"));
                        FantasyTeam team = new FantasyTeam();
                        team.setTeamId(teamId);

                        UUID playerId = UUID.fromString(rs.getString("playerId"));
                        Player player = new Player();
                        player.setPlayerId(playerId);

                        Fixture fixture = new Fixture();
                        fixture.setFixtureId(fixtureId);

                        String scoringRuleId = rs.getString("ruleId");
                        ScoringRule rule = null;
                        if (scoringRuleId != null){
                            rule = new ScoringRule();
                            rule.setRuleId(UUID.fromString(scoringRuleId));
                        }

                        FantasyPoints fp = FantasyPoints.builder()
                                .pointsId(UUID.fromString(rs.getString("pointsId")))
                                .pointsEarned(rs.getInt("pointsEarned"))
                                .calculationDate(rs.getObject("calculationDate", LocalDateTime.class))
                                .matchRoundNumber(rs.getInt("match_round_number"))
                                .calculationVersion(rs.getInt("calculationVersion"))
                                .fantasyTeam(team)
                                .player(player)
                                .fixture(fixture)
                                .scoringRule(rule)
                                .build();
                        fantasyPoints.add(fp);
                    }
                }
            } catch (SQLException e) {
                LOG.log(Level.SEVERE, "Could not find any fantasy points by fixture ID", e);
                throw new DataAccessException("Could not find any fantasy points by fixture ID", e);
            }
            return fantasyPoints;
    }

    @Override
    public List<FantasyPoints> findByTeamId(UUID teamId) {
        return List.of();
    }
}
