package com.vzap.trytons.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.logging.Logger;

@Path("/admin/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class AdminUserResource {

    private static final Logger LOGGER = Logger.getLogger(AdminUserResource.class.getName());

    //@Inject
    // the service layer

    //your methods

}
