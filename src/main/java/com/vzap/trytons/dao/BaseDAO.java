package com.vzap.trytons.dao;

import com.vzap.trytons.util.DBConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseDAO {
    protected Connection getConnection() throws SQLException {
        return DBConnectionManager.getConnection();
    }

    protected void closeResources(Connection con, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace(); //Error should actually be logged.
            }
        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace(); //Error should actually be logged.
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace(); //Error should actually be logged.
            }
        }
    }
}
