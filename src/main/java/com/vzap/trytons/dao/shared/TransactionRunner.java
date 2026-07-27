package com.vzap.trytons.dao.shared;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.util.DBConnectionManager;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class TransactionRunner {

    private static final Logger LOG = Logger.getLogger(TransactionRunner.class.getName());

    public <T> T runInTransaction(TransactionalWork<T> work) {
        Connection con = null;

        try {
            con = DBConnectionManager.getConnection();
            con.setAutoCommit(false);

            T result = work.execute(con);
            con.commit();
            return result;

        } catch (SQLException e) {
            rollback(con, e);
            LOG.log(Level.SEVERE, "Transaction failed and was rolled back", e);
            throw new DataAccessException("Transaction failed and was rolled back", e);

        } catch (RuntimeException e) {
            rollback(con, e);
            LOG.log(Level.SEVERE, "Transaction failed and was rolled back", e);
            throw e;

        } finally {
            closeQuietly(con);
        }
    }

    private void rollback(Connection con, Throwable e) {
        if (con == null) {
            return;
        }

        try {
            con.rollback();
        } catch (SQLException rollbackException) {
            e.addSuppressed(rollbackException);
        }
    }

    private void closeQuietly(Connection con) {
        if (con == null) {
            return;
        }

        try {
            con.setAutoCommit(true);
            con.close();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not close database connection", e);
        }
    }
}
