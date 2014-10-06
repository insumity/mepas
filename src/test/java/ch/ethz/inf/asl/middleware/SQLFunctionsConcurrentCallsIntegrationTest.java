package ch.ethz.inf.asl.middleware;


import ch.ethz.inf.asl.utils.Utilities;
import org.postgresql.util.PSQLException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.*;

import static ch.ethz.inf.asl.middleware.IntegrationTest.getConnection;
import static ch.ethz.inf.asl.middleware.MWMessagingProtocolImpl.RECEIVE_MESSAGE;
import static ch.ethz.inf.asl.middleware.MWMessagingProtocolImpl.SEND_MESSAGE;
import static ch.ethz.inf.asl.utils.TestConstants.INTEGRATION;
import static org.testng.Assert.assertEquals;

/**
 * Tests for testing that when the stored procedures of the basic functionality of the system
 * are being called concurrently everything works as expected.
 */
public class SQLFunctionsConcurrentCallsIntegrationTest {

    // the state code is handling serialization failures (which always return with a SQLSTATE value of '40001'), becau
    // you retrieve the errorMessage and then get compare it to "40001" from there. Cha cha :)
    // constants of the database
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "";
    private static final String HOST = "localhost";
    private static final Integer PORT_NUMBER = 5432;
    private static final String DB_NAME = "integrationtest";


    private static final String INITIALIZE_DATABASE = "{ call initialize_database() }";

    @BeforeMethod(groups = INTEGRATION)
    public void initialize() throws ClassNotFoundException, SQLException, IOException, InterruptedException {
        IntegrationTest.initialize(false);
    }

    @AfterMethod(groups = INTEGRATION)
    public void tearDown() throws SQLException, ClassNotFoundException {
        IntegrationTest.tearDown();
    }

    private void addMessagesWithNullReceiverToTheSystem(int numberOfMessages, int senderId, int queueId)
            throws SQLException, ClassNotFoundException {

        for (int i = 0; i < numberOfMessages; ++i) {
            try (Connection connection = getConnection(DB_NAME);
                CallableStatement stmt = connection.prepareCall(SEND_MESSAGE)) {

                Timestamp arrivalTime = Timestamp.valueOf("2014-12-12 12:34:12");
                String message = Utilities.createStringWith(200, 'A');
                stmt.setInt(1, senderId);
                stmt.setNull(2, Types.INTEGER);
                stmt.setInt(3, queueId);
                stmt.setTimestamp(4, arrivalTime);
                stmt.setString(5, message);
                stmt.execute();
            }
        }
    }

    @Test(groups = INTEGRATION, description = "The idea of the test is to fill the system with " +
            "many messages, e.g. X messages, with receiver id being NULL and then create some concurrent readers " +
            "that try to read these messages. In total the concurrent readers should have only read X messages. ")
    public void testMessagesAreReceivedOnlyOnce() throws SQLException, ClassNotFoundException, InterruptedException {
        final String CONCURRENT_UPDATE_ERROR_SQL_STATE = "40001";
        final int NUMBER_OF_MESSAGES = 2000;

        final int NUMBER_OF_CONCURRENT_READERS = 4;

        // readMessagesByReader[i] contains the messages the i-th reader read
        final int[] readMessagesByReader = new int[NUMBER_OF_CONCURRENT_READERS];

        int senderId = 6;
        // should always be greater than NUMBER_OF_CONCURRENT_READERS since a reader doesn't read messages
        // he sent. TODO .. not really -1 or something??
        assert(NUMBER_OF_CONCURRENT_READERS < senderId);

        int queueId = 1;
        addMessagesWithNullReceiverToTheSystem(NUMBER_OF_MESSAGES, senderId, queueId);

        class ConcurrentReader implements Runnable {
            private int id;

            public ConcurrentReader(int id) {
                this.id = id;
            }

            @Override
            public void run() {
                // at maximum you are going to read NUMBER_OF_MESSAGES messages
                for (int i = 0; i < NUMBER_OF_MESSAGES; ++i) {
                    try (Connection connection = getConnection(DB_NAME)) {
//                        connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

                        try (CallableStatement stmt = connection.prepareCall(RECEIVE_MESSAGE)) {

                            boolean retrieveByArrivalTime = false;
                            // TODO this is buggy ... should I verify requestingUserId is in the valid range
                            // ???
                            stmt.setInt(1, (id + 1));
                            stmt.setInt(2, 1);
                            stmt.setBoolean(3, retrieveByArrivalTime);

                            ResultSet rs = null;
                            try {
                                rs = stmt.executeQuery();
                            } catch (PSQLException e) {
                                if (e.getServerErrorMessage().getSQLState().equals(CONCURRENT_UPDATE_ERROR_SQL_STATE)) {
                                    stmt.close();
                                    connection.close();
                                    continue;
                                }
                            }
                            boolean thereAreRows = rs.next();
                            if (thereAreRows) {
                                readMessagesByReader[id]++;
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        ConcurrentReader[] readers = new ConcurrentReader[NUMBER_OF_CONCURRENT_READERS];
        Thread[] threadReaders = new Thread[NUMBER_OF_CONCURRENT_READERS];
        for (int i = 0; i < NUMBER_OF_CONCURRENT_READERS; ++i) {
            readers[i] = new ConcurrentReader(i);
            threadReaders[i] = new Thread(readers[i]);
            threadReaders[i].start();
        }

        // wait till all readers finish
        for (int i = 0; i < NUMBER_OF_CONCURRENT_READERS; ++i) {
            threadReaders[i].join();
        }

        int totalReadMessages = 0;
        for (int i = 0; i < NUMBER_OF_CONCURRENT_READERS; ++i) {
            totalReadMessages += readMessagesByReader[i];
        }

        assertEquals(totalReadMessages, NUMBER_OF_MESSAGES);
    }
}
