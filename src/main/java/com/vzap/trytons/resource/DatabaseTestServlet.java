package com.vzap.trytons.resource;

import jakarta.servlet.http.HttpServlet;

/**
 * Deprecated diagnostic endpoint, de-routed under W4-BUG-BE-19.
 *
 * This duplicated the database health check outside the Resource -> Service
 * -> DAO layering and forwarded raw table names and SQL error text to a JSP.
 * The single supported, layered health contract is
 * {@link com.vzap.trytons.service.DatabaseHealthResource} (GET /test/database).
 *
 * The {@code @WebServlet} mapping has been removed so this class is no
 * longer routable; it is intentionally left unmapped rather than deleted.
 */
public class DatabaseTestServlet extends HttpServlet {
}
