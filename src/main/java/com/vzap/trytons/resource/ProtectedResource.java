package com.vzap.trytons.resource;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Context;

public class ProtectedResource {
    @Context
    private HttpServlet httpServlet;
}
