package com.vzap.trytons.dao.shared;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionalWork<T> {
    T execute(Connection con) throws SQLException;
}
