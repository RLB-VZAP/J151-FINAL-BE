package com.vzap.trytons.dao.shared;

import com.vzap.trytons.exceptions.ApplicationException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.util.DBConnectionManager;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Runs a unit of work across several DAO calls on a single pooled connection so that
 * they commit or roll back together. DAOs expose {@code Connection}-taking overloads
 * for exactly this purpose; the no-connection overloads stay auto-commit.
 */
@ApplicationScoped
public class TransactionRunner {

    @FunctionalInterface
    public interface TransactionalWork<T> {
        T execute(Connection con) throws SQLException;
    }

    public <T> T runInTransaction(TransactionalWork<T> work) {
        Connection con = null;
        boolean previousAutoCommit = true;

        try {
            con = DBConnectionManager.getConnection();
            previousAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            T result = work.execute(con);

            con.commit();
            return result;
        } catch (SQLException e) {
            rollbackQuietly(con, e);
            throw new DataAccessException("Transaction failed and was rolled back", e);
        } catch (ApplicationException e) {
            rollbackQuietly(con, e);
            throw e;
        } catch (RuntimeException e) {
            rollbackQuietly(con, e);
            throw new DataAccessException("Transaction failed and was rolled back", e);
        } finally {
            closeQuietly(con, previousAutoCommit);
        }
    }

    public void runInTransaction(VoidTransactionalWork work) {
        runInTransaction(con -> {
            work.execute(con);
            return null;
        });
    }

    @FunctionalInterface
    public interface VoidTransactionalWork {
        void execute(Connection con) throws SQLException;
    }

    private void rollbackQuietly(Connection con, Exception cause) {
        if (con == null) {
            return;
        }

        try {
            con.rollback();
        } catch (SQLException rollbackException) {
            cause.addSuppressed(rollbackException);
        }
    }

    private void closeQuietly(Connection con, boolean previousAutoCommit) {
        if (con == null) {
            return;
        }

        try {
            con.setAutoCommit(previousAutoCommit);
        } catch (SQLException ignored) {
            // Connection is being returned to the pool regardless
        }

        try {
            con.close();
        } catch (SQLException ignored) {
            // Nothing useful can be done when returning the connection fails
        }
    }
}
