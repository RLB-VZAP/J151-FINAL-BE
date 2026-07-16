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

        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES ")) {
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
        }

        return tables;
    }
}

