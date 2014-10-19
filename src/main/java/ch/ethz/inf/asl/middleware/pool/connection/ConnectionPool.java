package ch.ethz.inf.asl.middleware.pool.connection;

import ch.ethz.inf.asl.exceptions.ConnectionPoolException;
import org.postgresql.ds.PGPoolingDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static ch.ethz.inf.asl.utils.Verifier.*;

// FIXME
public class ConnectionPool {
//
//    class InternalConnection implements Connection {
// TODO fixme
//    }

    private BlockingQueue<Connection> connections;
    private PGPoolingDataSource dataSource;

    public ConnectionPool(String dataSourceName, String host, int portNumber, String username, String password, String databaseName, int initialConnections, int maximumConnections) {
        connections = new ArrayBlockingQueue<>(maximumConnections);
        hasText(host, "Given host cannot be empty or null!");
        verifyTrue(portNumber >= 0, "Given portNumber cannot be negative!");
        hasText(username, "Given username cannot be empty or null!");

        // password could possibly be empty, e.g. when running the pool locally
        notNull(password, "Given password cannot be null!");

        hasText(databaseName, "Given databaseName cannot be empty or null!");
        verifyTrue(initialConnections >= 0, "initialConnections cannot be negative!");
        verifyTrue(maximumConnections >= 0, "maximumConnections cannot be negative!");
        verifyTrue(initialConnections <= maximumConnections, "initialConnections cannot be greater than maximumConnections!");

        // initialize connection pool (from: http://jdbc.postgresql.org/documentation/head/ds-ds.html)
        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName(dataSourceName);
        source.setServerName(host);
        source.setPortNumber(portNumber);
        source.setDatabaseName(databaseName);
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

//        return connections.take();
//        try {
//            Connection connection = DriverManager.getConnection(
//                    "jdbc:postgresql://127.0.0.1:5432/testdb", "mkyong",
//                    "123456");
//            connections.put(connection);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new ConnectionPoolException("Couldn't retrieve connection from the connection pool!", e);
        }
    }

}
