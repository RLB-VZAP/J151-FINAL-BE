package com.vzap.trytons.resource.market;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.service.market.MarketDemandService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/market-dashboard")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class MarketDashboardResource {

    @Inject
    private MarketDemandService marketDemandService;

    @GET
    public Response getDashboard() {
        return Response.ok(marketDemandService.getDashboard()).build();
    }
}
