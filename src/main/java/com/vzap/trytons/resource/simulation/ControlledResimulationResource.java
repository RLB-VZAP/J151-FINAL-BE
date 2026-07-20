package com.vzap.trytons.resource.simulation;

import com.vzap.trytons.dto.simulation.ResimulationRequestDTO;
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
        // TODO (W3-BE-DATABASE-LOGIC-FIX-05A): Resolve the authenticated administrator, call ControlledResimulationServiceImpl.resimulateFixture with their id and the request, and return the resulting ResimulationResponseDTO.
    }

    @GET
    @Path("/fixture/{fixtureId}")
    public Response listResimulationsForFixture(@PathParam("fixtureId") UUID fixtureId) {
        // TODO (W3-BE-DATABASE-LOGIC-FIX-05A): Call ControlledResimulationServiceImpl.listResimulationsForFixture with the given fixtureId and return the list of ResimulationResponseDTO.
    }
}