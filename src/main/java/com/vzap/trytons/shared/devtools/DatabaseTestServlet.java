//TODO Needs to be redone
package com.vzap.trytons.shared.devtools;

import com.vzap.trytons.shared.dao.DatabaseTestDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/db-test")
public class DatabaseTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        DatabaseTestDAO dao = new DatabaseTestDAO();
        try {
            List<String> tables = dao.getTableNames();

            if (tables.isEmpty()) {
                req.setAttribute("status", "WARNING");
                req.setAttribute("message", "Connection succeeded but no tables were found." + "Has schema.sql been applied?");
            } else {
                req.setAttribute("status", "SUCCESS");
                req.setAttribute("message", "Connection established." + tables.size() + " tables found in tryton_fantasy_rugby.");
            }
            req.setAttribute("tables", tables);

        } catch (SQLException e) {
            req.setAttribute("status", "ERROR");
            req.setAttribute("message", "database connection failed: " + e.getMessage());
            req.setAttribute("tables", List.of());
        }
        req.getRequestDispatcher("/database-test.jsp").forward(req, resp);
    }
}
