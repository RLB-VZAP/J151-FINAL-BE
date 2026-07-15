package com.vzap.trytons.dao;

import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.FantasyRound;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Fixture;
import com.vzap.trytons.model.League;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FixtureDAOImpl extends BaseDAO implements FixtureDAO {
    private static final Logger LOG = Logger.getLogger(FixtureDAOImpl.class.getName());
    @Override
    public Fixture create(Fixture fixture) {
        String query = "INSERT INTO fixture " +
                "(fixtureId, leagueId, roundId, team_a_id, team_b_id, fixtureDate, fixtureTime, status, simulationDate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,fixture.getFixtureId().toString());
            ps.setString(2,fixture.getLeagueId().getLeagueId().toString());
            ps.setString(3,fixture.getRoundId().getRoundId().toString());
            ps.setString(4,fixture.getTeamA().getTeamId().toString());
            ps.setString(5,fixture.getTeamB().getTeamId().toString());
            ps.setDate(6,Date.valueOf(fixture.getFixtureDate()));
            ps.setTime(7,Time.valueOf(fixture.getFixtureTime()));
            ps.setString(8,fixture.getStatus().toString());
            if(fixture.getSimulationDate() != null){
                ps.setTimestamp(9,Timestamp.valueOf(fixture.getSimulationDate()));
            }else{
                ps.setNull(9,Types.TIMESTAMP);
            }
            if(ps.executeUpdate() == 1){
                Optional<Fixture> createdFixture = findById(fixture.getFixtureId());
                if(createdFixture.isPresent()){
                    return createdFixture.get();
                }
                throw new DataAccessException("Fixture was inserted, but cannot be retrieved.",null);
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to create fixture", e);
            throw new DataAccessException("Unable to create fixture", e);
        }
        return null;
    }

    @Override
    public Optional<Fixture> findById(UUID fixtureId) {
        String query = "SELECT * FROM fixture WHERE fixtureId = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,fixtureId.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return Optional.of(this.mapFixture(rs));
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to find fixture", e);
            throw new DataAccessException("Unable to find fixture", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Fixture> findByLeagueId(UUID leagueId) {
        String query = "SELECT * FROM fixture WHERE leagueId = ?";
        List<Fixture> fixtures = new ArrayList<>();
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,leagueId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                fixtures.add(this.mapFixture(rs));
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to find fixture", e);
            throw new DataAccessException("Unable to find fixture", e);
        }
        return fixtures;
    }

    @Override
    public List<Fixture> findByRoundId(UUID roundId) {
        String query = "SELECT * FROM fixture WHERE roundId = ?";
        List<Fixture> fixtures = new ArrayList<>();
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,roundId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                fixtures.add(this.mapFixture(rs));
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find fixture", e);
            throw new DataAccessException("Unable to find fixture", e);
        }
        return fixtures;
    }

    @Override
    public List<Fixture> findByTeamId(UUID teamId) {
        String query = "SELECT * FROM fixture WHERE team_a_id = ? OR team_b_id = ?";
        List<Fixture> fixtures = new ArrayList<>();
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,teamId.toString());
            ps.setString(2,teamId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                fixtures.add(this.mapFixture(rs));
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to find fixture", e);
            throw new DataAccessException("Unable to find fixture", e);
        }
        return fixtures;
    }

    @Override
    public List<Fixture> findByStatus(FixtureStatus status) {
        String query = "SELECT * FROM fixture WHERE status = ?";
        List<Fixture> fixtures = new ArrayList<>();
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,status.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                fixtures.add(this.mapFixture(rs));
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to find fixture", e);
            throw new DataAccessException("Unable to find fixture", e);
        }
        return fixtures;
    }

    @Override
    public List<Fixture> getAllFixtures() {
        String query = "SELECT * FROM fixture";
        List<Fixture> fixtures = new ArrayList<>();
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                fixtures.add(this.mapFixture(rs));
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to find fixture", e);
            throw new DataAccessException("Unable to find fixture", e);
        }
        return fixtures;
    }

    @Override
    public boolean updateFixture(Fixture fixture) {
        String query = "UPDATE fixture SET leagueId=?, roundId=?, team_a_id=?, team_b_id=?, fixtureDate=?, fixtureTime=?, status=?, simulationDate=? WHERE fixtureId=?" ;
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,fixture.getLeagueId().getLeagueId().toString());
            ps.setString(2,fixture.getRoundId().getRoundId().toString());
            ps.setString(3,fixture.getTeamA().getTeamId().toString());
            ps.setString(4,fixture.getTeamB().getTeamId().toString());
            ps.setDate(5,Date.valueOf(fixture.getFixtureDate()));
            ps.setTime(6,Time.valueOf(fixture.getFixtureTime()));
            ps.setString(7,fixture.getStatus().toString());
            if(fixture.getSimulationDate() != null){
                ps.setTimestamp(8,Timestamp.valueOf(fixture.getSimulationDate()));
            }else{
                ps.setNull(8,Types.TIMESTAMP);
            }
            ps.setString(9,fixture.getFixtureId().toString());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to update fixture", e);
            throw new DataAccessException("Unable to update fixture", e);
        }
    }

    @Override
    public boolean cancelFixture(UUID fixtureId) {
        String query =  "UPDATE fixture SET status='CANCELLED' WHERE fixtureId=?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,fixtureId.toString());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to cancel fixture", e);
            throw new DataAccessException("Unable to cancel fixture", e);
        }
    }

    @Override
    public boolean updateStatus(Fixture fixture, FixtureStatus status) {
        String query = "UPDATE fixture SET status=? WHERE fixtureId=?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,status.toString());
            ps.setString(2,fixture.getFixtureId().toString());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to update fixture", e);
            throw new DataAccessException("Unable to update fixture", e);
        }
    }
    private Fixture mapFixture(ResultSet rs) throws SQLException {
        Fixture fixture = new Fixture();
        fixture.setFixtureId(UUID.fromString(rs.getString("fixtureId")));
        League league = new League();
        league.setLeagueId(UUID.fromString(rs.getString("leagueId")));
        fixture.setLeagueId(league);
        FantasyRound round = new FantasyRound();
        round.setRoundId(UUID.fromString(rs.getString("roundId")));
        fixture.setRoundId(round);
        FantasyTeam teamA = new FantasyTeam();
        teamA.setTeamId(UUID.fromString(rs.getString("team_a_id")));
        fixture.setTeamA(teamA);
        FantasyTeam teamB = new FantasyTeam();
        teamB.setTeamId(UUID.fromString(rs.getString("team_b_id")));
        fixture.setTeamB(teamB);
        fixture.setStatus(FixtureStatus.valueOf(rs.getString("status")));
        fixture.setFixtureDate(rs.getDate("fixtureDate").toLocalDate());
        fixture.setFixtureTime(rs.getTime("fixtureTime").toLocalTime());
        Timestamp simulationTimestamp = rs.getTimestamp("simulationDate");
        if (simulationTimestamp != null) {
            fixture.setSimulationDate(simulationTimestamp.toLocalDateTime());
        }
        fixture.setCreatedAt(
                rs.getTimestamp("createdAt").toLocalDateTime()
        );
        return fixture;
    }
}
