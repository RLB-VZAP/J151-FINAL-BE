package com.vzap.trytons.resource.catalog;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.pricing.PlayerPriceHistoryDTO;
import com.vzap.trytons.service.pricing.PricingService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@RequestScoped
@Path("/player-prices")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class PlayerPriceHistoryResource {

    @Inject
    private PricingService pricingService;

    @GET
    @Path("/{playerId}/history")
    public Response getPlayerHistory(@PathParam("playerId") UUID playerId,
                                     @QueryParam("limit") @DefaultValue("20") int limit) {
        List<PlayerPriceHistoryDTO> history = pricingService.getPlayerHistory(playerId, limit);
        return Response.ok(history).build();
    }
}
