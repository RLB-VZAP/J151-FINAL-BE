package com.vzap.trytons.resource;

import com.vzap.trytons.dto.ResimulationRequestDTO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@RequestScoped
@Path("/resimulations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ControlledResimulationResource {

    @POST
    public Response resimulateFixture(ResimulationRequestDTO request) {
        throw new UnsupportedOperationException("ControlledResimulationResource.resimulateFixture is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after ControlledResimulationServiceImpl orchestration rules are confirmed.");
    }

    @GET
    @Path("/fixture/{fixtureId}")
    public Response listResimulationsForFixture(@PathParam("fixtureId") UUID fixtureId) {
        throw new UnsupportedOperationException("ControlledResimulationResource.listResimulationsForFixture is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after result-history and resimulation response mapping are confirmed.");
    }
}