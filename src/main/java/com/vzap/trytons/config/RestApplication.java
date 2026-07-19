package com.vzap.trytons.config;

import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.filter.RoleFilter;
import com.vzap.trytons.mapper.ApplicationExceptionMapper;
import com.vzap.trytons.mapper.JAXExceptionMapper;
import com.vzap.trytons.resource.*;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.Set;

@ApplicationPath("/api")
public class RestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                AdminUserResource.class,
                AuthResource.class,
                ClubResource.class,
                ControlledResimulationResource.class,
                FantasyPointBreakdownResource.class,
                FantasyPointsResource.class,
                FantasyTeamResource.class,
                FixtureResource.class,
                LeaderboardResource.class,
                LeagueResource.class,
                LockStatusResource.class,
                MatchResultResource.class,
                MatchTeamScoreResource.class,
                NotificationResource.class,
                PlayerResource.class,
                PlayerStatisticsResource.class,
                PositionResource.class,
                ScoringRuleResource.class,
                SimulationSettingResource.class,
                TransferRecommendationResource.class,
                TransferResource.class,
                UserHistoryResource.class,
                UserResources.class,

                AuthFilter.class,
                RoleFilter.class,
                ApplicationExceptionMapper.class,
                JAXExceptionMapper.class
        );
    }
}
