package com.vzap.trytons.dao.leaderboard;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.enums.LeagueType;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.util.LeaderboardAggregator;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class LeaderboardAggregationDAOImpl extends BaseDAO implements LeaderboardAggregationDAO {

    private static final Logger LOG = Logger.getLogger(LeaderboardAggregationDAOImpl.class.getName());

    // One row per team per decided fixture: the team's own match_team_score
    // total, and its opponent's, found via a self-join on the same resultId.
    // isCurrent = TRUE keeps re-simulated fixtures from double counting an
    // old run.
    //
    // No fixture.status predicate: a row only reaches this query once it has
    // a match_team_score, and match_team_score is only ever written by the
    // processing pipeline (TeamScoreServiceImpl.updateTeamScoresForFixture),
    // so the join itself is already the "has this been scored" guard. Adding
    // a status filter on top of that is redundant at best and actively wrong
    // in practice: MatchProcessingServiceImpl advances a fixture to
    // PROCESSED after scoring it, but seeded data (seed-presentation.sql)
    // writes match_team_score rows against fixtures left at COMPLETED, so a
    // literal status check excluded every seeded result and made the whole
    // aggregation return nothing.
    private static final String BASE_QUERY =
            "SELECT mts.teamId AS teamId, l.leagueType AS leagueType, "
            + "mts.totalScore AS pointsFor, opp.totalScore AS pointsAgainst "
            + "FROM match_team_score mts "
            + "JOIN match_team_score opp ON opp.resultId = mts.resultId AND opp.teamId <> mts.teamId "
            + "JOIN matchResult mr ON mr.resultId = mts.resultId AND mr.isCurrent = TRUE "
            + "JOIN fixture f ON f.fixtureId = mr.fixtureId "
            + "JOIN league l ON l.leagueId = f.leagueId "
            + "JOIN fantasyRound r ON r.roundId = f.roundId "
            + "WHERE r.season = ?";

    // This clause is the whole feature: it is what keeps a private league's
    // friendlies out of the master total.
    private static final String MASTER_QUERY = BASE_QUERY + " AND l.leagueType = 'PUBLIC'";

    // Deliberately no leagueType filter: a private league still counts its
    // own matches in its own table.
    private static final String LEAGUE_QUERY = BASE_QUERY + " AND f.leagueId = ?";

    @Override
    public List<LeaderboardAggregator.FixtureScoreRow> findMasterScoreRows(String season) {
        List<LeaderboardAggregator.FixtureScoreRow> rows = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(MASTER_QUERY)) {
            ps.setString(1, season);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to aggregate master leaderboard score rows for season " + season, e);
            throw new DataAccessException("Unable to aggregate master leaderboard score rows for season " + season, e);
        }
        return rows;
    }

    @Override
    public List<LeaderboardAggregator.FixtureScoreRow> findLeagueScoreRows(String season, UUID leagueId) {
        List<LeaderboardAggregator.FixtureScoreRow> rows = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(LEAGUE_QUERY)) {
            ps.setString(1, season);
            ps.setString(2, leagueId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to aggregate league leaderboard score rows for league " + leagueId, e);
            throw new DataAccessException("Unable to aggregate league leaderboard score rows for league " + leagueId, e);
        }
        return rows;
    }

    private static LeaderboardAggregator.FixtureScoreRow mapRow(ResultSet rs) throws SQLException {
        return new LeaderboardAggregator.FixtureScoreRow(
                UUID.fromString(rs.getString("teamId")),
                LeagueType.valueOf(rs.getString("leagueType")),
                rs.getInt("pointsFor"),
                rs.getInt("pointsAgainst"));
    }
}
