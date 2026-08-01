package com.vzap.trytons.service.shared;

import com.vzap.trytons.dao.fixture.FantasyRoundDAO;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.model.fixture.FantasyRound;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Comparator;
import java.util.Optional;

/**
 * Resolves "the current season" the same way everywhere it is needed:
 * whichever fantasyRound is currently OPEN, or failing that the latest
 * season/round the calendar has ever defined. Neither a league nor a
 * leaderboard carries its own season, so anything that needs to key off one
 * -- starting a tournament, refreshing a leaderboard, creating a league's
 * board -- has to ask this same question.
 *
 * <p>Originally lived only in {@code TournamentServiceImpl.resolveSeason()};
 * lifted out here once {@code LeaderboardServiceImpl} and
 * {@code LeagueServiceImpl} needed the identical answer, so the three
 * services can't drift into three slightly different tie-break rules.
 */
@ApplicationScoped
public class SeasonResolver {

    @Inject
    private FantasyRoundDAO fantasyRoundDAO;

    public String resolveCurrentSeason() {
        Optional<FantasyRound> open = fantasyRoundDAO.getCurrentOpenRound();
        if (open.isPresent()) {
            return open.get().getSeason();
        }

        return fantasyRoundDAO.getAllRounds().stream()
                .max(Comparator.comparing(FantasyRound::getSeason)
                        .thenComparingInt(FantasyRound::getRoundNumber))
                .map(FantasyRound::getSeason)
                .orElseThrow(() -> new BusinessRuleException(
                        "No fantasy season has been configured yet."));
    }
}
