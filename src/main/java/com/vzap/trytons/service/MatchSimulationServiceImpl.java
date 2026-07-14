package com.vzap.trytons.service;

import com.vzap.trytons.dao.FixtureDAO;
import com.vzap.trytons.dao.RegisteredUserDAO;
import com.vzap.trytons.dao.UserDAO;
import com.vzap.trytons.dto.MatchResultResponseDTO;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Fixture;
import com.vzap.trytons.model.RegisteredUser;
import com.vzap.trytons.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class MatchSimulationServiceImpl implements MatchSimulationService {
    private static final Logger LOG = Logger.getLogger(MatchProcessingServiceImpl.class.getName());

    @Inject
    private RegisteredUserDAO registeredUserDAO;
    @Inject
    private FixtureDAO fixtureDAO;

    @Override
    public MatchResultResponseDTO simulateFixture(UUID actorUserId, UUID fixtureId) {
            User user = registeredUserDAO.getRegisteredUserById(actorUserId).orElseThrow(()-> new AuthorisationException("User not found"));
            Fixture fixture = fixtureDAO.findById(fixtureId).orElseThrow(()-> new ResourceNotFoundException("Fixture not found"));
            FantasyTeam teamA =  fixture.getTeamA();
            FantasyTeam teamB =  fixture.getTeamB();

    }
}
