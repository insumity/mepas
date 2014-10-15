package ch.ethz.inf.asl.middleware.pool.connection;

import ch.ethz.inf.asl.exceptions.ConnectionPoolException;
import org.postgresql.ds.PGPoolingDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static ch.ethz.inf.asl.utils.Helper.hasText;
import static ch.ethz.inf.asl.utils.Helper.notNull;
import static ch.ethz.inf.asl.utils.Helper.verifyTrue;

// FIXME
public class ConnectionPool {

    private PGPoolingDataSource dataSource;

    public ConnectionPool(String host, int portNumber, String username, String password, String db, int initialConnections, int maximumConnections) {
        hasText(host, "Given host cannot be empty or null");
        verifyTrue(portNumber >= 0, "Given portNumber cannot be negative");
        hasText(username, "Given username cannot be empty or null");

        // password could possibly be empty, e.g. when running the pool locally
        notNull(password, "Given password cannot be null");
        verifyTrue(initialConnections >= 0, "initialConnections cannot be negative");
        verifyTrue(initialConnections <= maximumConnections, "initialConnections cannot be greater than maximumConnections");

        // initialize connection pool (from: http://jdbc.postgresql.org/documentation/head/ds-ds.html)
        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("connection pool");
        source.setServerName(host);
        source.setPortNumber(portNumber);
        source.setDatabaseName(db);
        source.setUser(username);
        source.setPassword(password);
        source.setInitialConnections(initialConnections);
        source.setMaxConnections(maximumConnections);

        try {
            source.initialize();
        } catch (SQLException e) {
            throw new ConnectionPoolException("Couldn't initialize the connection pool!", e);
        }

        dataSource = source;
    }

    // SHOULD PROBABLY BE SYNCHRONIZED
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new ConnectionPoolException("Couldn't retrieve connection from the connection pool!", e);
        }
    }

}
