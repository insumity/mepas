package ch.ethz.inf.asl.middleware.pool.connection;

import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;

public class ConnectionPoolTest {

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = ".*host cannot be empty or null.*")
    public void testConnectionPoolWithNullHost() {
        new ConnectionPool(null, 8344, "username", "password", "dbName", 5, 5);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*host cannot be empty or null.*")
    public void testConnectionPoolWithEmptyHost() {
        new ConnectionPool("", 8344, "username", "password", "dbName", 5, 5);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*portNumber cannot be negative.*")
    public void testConnectionPoolWithNegativePortNumber() {
        new ConnectionPool("host", -1, "username", "password", "dbName", 5, 5);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = ".*username cannot be empty or null.*")
    public void testConnectionPoolWithNullUsername() {
        new ConnectionPool("host", 8344, null, "password", "dbName", 5, 5);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*username cannot be empty or null.*")
    public void testConnectionPoolWithEmptyUsername() {
        new ConnectionPool("host", 8344, "", "password", "dbName", 5, 5);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = ".*password cannot be null.*")
    public void testConnectionPoolWithNullPassword() {
        new ConnectionPool("host", 8344, "username", null, "dbName", 5, 5);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = ".*databaseName cannot be empty or null.*")
    public void testConnectionPoolWithNullDatabaseName() {
        new ConnectionPool("host", 8344, "username", "password", null, 5, 5);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*databaseName cannot be empty or null.*")
    public void testConnectionPoolWithEmptyDatabaseName() {
        new ConnectionPool("host", 8344, "username", "password", "", 5, 5);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*initialConnections cannot be negative.*")
    public void testConnectionPoolWithNegativeInitialConnections() {
        new ConnectionPool("host", 43, "username", "password", "dbName", -5, 5);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*maximumConnections cannot be negative.*")
    public void testConnectionPoolWithNegativeMaximumConnections() {
        new ConnectionPool("host", 43, "username", "password", "dbName", 0, -5);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*initialConnections cannot be greater than maximumConnections.*")
    public void testConnectionPoolWithInitialConnectionsGreaterThanMaximumConnections() {
        new ConnectionPool("host", 43, "username", "password", "dbName", 6, 5);
    }


}
