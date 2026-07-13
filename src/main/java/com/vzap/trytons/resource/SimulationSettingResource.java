package com.vzap.trytons.resource;

import com.vzap.trytons.dto.SimulationSettingRequestDTO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@RequestScoped
@Path("/simulation-settings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SimulationSettingResource {

    @POST
    public Response createSimulationSetting(SimulationSettingRequestDTO request) {

        throw new UnsupportedOperationException("SimulationSettingResource.createSimulationSetting is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after SimulationSettingServiceImpl validation and persistence rules are confirmed.");
    }

    @PUT
    @Path("/{simulationSettingsId}")
    public Response updateSimulationSetting(@PathParam("simulationSettingsId") UUID simulationSettingsId, SimulationSettingRequestDTO request) {

        throw new UnsupportedOperationException("SimulationSettingResource.updateSimulationSetting is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after SimulationSettingServiceImpl update and active-setting rules are confirmed.");
    }

    @GET
    @Path("/{simulationSettingsId}")
    public Response getSimulationSettingById(@PathParam("simulationSettingsId") UUID simulationSettingsId) {

        throw new UnsupportedOperationException("SimulationSettingResource.getSimulationSettingById is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after SimulationSettingServiceImpl read mapping is confirmed.");
    }

    @GET
    @Path("/active")
    public Response getActiveSimulationSetting() {

        throw new UnsupportedOperationException("SimulationSettingResource.getActiveSimulationSetting is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after active simulation setting selection rules are confirmed.");
    }

    @GET
    public Response listSimulationSettings() {

        throw new UnsupportedOperationException("SimulationSettingResource.listSimulationSettings is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after SimulationSettingServiceImpl list mapping is confirmed.");
    }
}
