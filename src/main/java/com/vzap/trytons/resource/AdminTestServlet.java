package com.vzap.trytons.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/admin/test")
public class AdminTestServlet extends HttpServlet {

    //If this code runs at all its proof that RoleFilter already confirmed
    //that the user is an Admin
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{\\\"success\\\": true, \\\"message\\\": \\\"Admin access confirmed\\\"}");
    }
}
