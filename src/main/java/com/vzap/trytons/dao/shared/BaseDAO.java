package com.vzap.trytons.dao.shared;

import com.vzap.trytons.util.DBConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseDAO {
    protected Connection getConnection() throws SQLException {
        return DBConnectionManager.getConnection();
    }

    protected void rollbackQuietly(Connection con, Exception cause) {
        if (con == null) {
            return;
        }

        try {
            con.rollback();
        } catch (SQLException rollbackException) {
            cause.addSuppressed(rollbackException);
        }
    }
}
