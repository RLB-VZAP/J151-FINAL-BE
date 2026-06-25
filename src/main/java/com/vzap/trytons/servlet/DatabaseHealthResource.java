package com.vzap.trytons.servlet;

import com.vzap.trytons.dao.DatabaseTestDAO;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/test/database")
public class DatabaseHealthResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseHealth() {
        DatabaseTestDAO dao = new DatabaseTestDAO();
        Map<String, Object> result = new HashMap<>();

        try {
            List<String> tables = dao.getTableNames();

            if (tables.isEmpty()) {
                result.put("message", "connection succeeded but no tables were found. has schema.sql been applied?");
            } else {
                result.put("status", "SUCCESS");
                result.put("message", "connection established. " + tables.size() + " tables found in tryton_fantasy_rugby.");
            }

           result.put("tables", tables);
            return Response.ok(result).build();

        } catch (SQLException e) {
            result.put("status", "ERROR");
            result.put("message","database connection failed:"+e.getMessage());
            result.put("tables",e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
        }
    }
}