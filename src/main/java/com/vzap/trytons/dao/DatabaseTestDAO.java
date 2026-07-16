package com.vzap.trytons.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class DatabaseTestDAO extends BaseDAO {
    public List<String> getTableNames() throws SQLException {
        List<String> tables = new ArrayList<>();

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("SHOW TABLES ");
            while (rs.next()) {
                tables.add(rs.getString(1));
            }

        } finally {
            closeResources(con, stmt, rs);
        }

        return tables;
    }
}

