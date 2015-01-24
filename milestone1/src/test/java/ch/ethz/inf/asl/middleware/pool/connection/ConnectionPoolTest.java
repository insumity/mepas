package ch.ethz.inf.asl.middleware.pool.connection;

import ch.ethz.inf.asl.testutils.InitializeDatabase;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static ch.ethz.inf.asl.testutils.TestConstants.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ConnectionPoolTest {

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = ".*host cannot be empty or null.*")
    public void testConnectionPoolWithNullHost() {
        new ConnectionPool(null, 8344, "username", "password", "dbName", 5);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*host cannot be empty or null.*")
    public void testConnectionPoolWithEmptyHost() {
        new ConnectionPool("", 8344, "username", "password", "dbName", 5);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*portNumber cannot be negative.*")
    public void testConnectionPoolWithNegativePortNumber() {
        new ConnectionPool("host", -1, "username", "password", "dbName", 5);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = ".*username cannot be empty or null.*")
    public void testConnectionPoolWithNullUsername() {
        new ConnectionPool("host", 8344, null, "password", "dbName", 5);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*username cannot be empty or null.*")
    public void testConnectionPoolWithEmptyUsername() {
        new ConnectionPool("host", 8344, "", "password", "dbName", 5);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = ".*password cannot be null.*")
    public void testConnectionPoolWithNullPassword() {
        new ConnectionPool("host", 8344, "username", null, "dbName", 5);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = ".*databaseName cannot be empty or null.*")
    public void testConnectionPoolWithNullDatabaseName() {
        new ConnectionPool("host", 8344, "username", "password", null, 5);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*databaseName cannot be empty or null.*")
    public void testConnectionPoolWithEmptyDatabaseName() {
        new ConnectionPool("host", 8344, "username", "password", "", 5);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*maximumConnections cannot be negative.*")
    public void testConnectionPoolWithNegativeMaximumConnections() {
        new ConnectionPool("host", 43, "username", "password", "dbName",  -5);
    }

    @Test(groups = DATABASE)
    public void testGetConnection() throws ClassNotFoundException, SQLException, InterruptedException, IOException {
        final String DATABASE_NAME = "connectionpooltest";

        InitializeDatabase.initializeDatabaseWithClientsAndQueues(HOST, PORT_NUMBER, DATABASE_NAME,
                USERNAME, PASSWORD, new String[]{}, 10, 10);

        ConnectionPool pool = new ConnectionPool(HOST, PORT_NUMBER, USERNAME, PASSWORD, DATABASE_NAME, 2);

        Connection connectionOne = pool.getConnection();
        int connectionOneHash = connectionOne.hashCode();

        Connection connectionTwo = pool.getConnection();
        int connectionTwoHash = connectionTwo.hashCode();

        assertNotNull(connectionOne);
        assertNotNull(connectionTwo);

        connectionTwo.close();

        // check that connections are re-used by checking the hash code of the returned connections
        Connection connection = pool.getConnection();
        assertEquals(connectionTwoHash, connection.hashCode());

        connectionOne.close();
        connection = pool.getConnection();
        assertEquals(connectionOneHash, connection.hashCode());
    }
}
