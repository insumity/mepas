package ch.ethz.inf.asl.middleware;


import ch.ethz.inf.asl.testutils.InitializeDatabase;
import org.postgresql.util.PSQLException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static ch.ethz.inf.asl.middleware.MiddlewareMessagingProtocolImpl.*;
import static ch.ethz.inf.asl.testutils.InitializeDatabase.getConnection;
import static ch.ethz.inf.asl.testutils.TestConstants.*;
import static org.testng.Assert.*;

/**
 * Tests for testing that when the stored procedures of the basic functionality of the system
 * are being called concurrently everything works as expected.
 */
public class SQLFunctionsConcurrentCallsDatabaseTest {

    private void addMessagesWithNullReceiverToTheSystem(int numberOfMessages, int senderId, int queueId)
            throws SQLException, ClassNotFoundException {

        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD)) {
             for (int i = 0; i < numberOfMessages; ++i) {
                try (CallableStatement stmt = connection.prepareCall(SEND_MESSAGE)) {

                    Date date = new java.util.Date();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String formattedDate = simpleDateFormat.format(date);
                    Timestamp arrivalTime = Timestamp.valueOf(formattedDate);
                    stmt.setInt(1, senderId);
                    stmt.setNull(2, Types.INTEGER);
                    stmt.setInt(3, queueId);
                    stmt.setTimestamp(4, arrivalTime);
                    stmt.setString(5, String.valueOf(i));
                    stmt.execute();
                }
            }
        }
    }

    @DataProvider(name = "twoIsolationLevels")
    public static Object[][] twoIsolationLevels() {
        return new Object[][] {
                {Connection.TRANSACTION_READ_COMMITTED, true},
                {Connection.TRANSACTION_READ_COMMITTED, false},
                {Connection.TRANSACTION_REPEATABLE_READ, true},
                {Connection.TRANSACTION_REPEATABLE_READ, false}
        };
    }


    @Test(groups = DATABASE, dataProvider = "twoIsolationLevels",
            description = "The idea of the test is to fill the system with " +
            "many messages, e.g. X messages, with receiver id being NULL and then create some concurrent readers " +
            "that try to read these messages. In total the concurrent readers should have only read X messages. ")
    public void testMessagesAreReceivedOnlyOnce(final int isolationLevel, final boolean fromSender)
            throws SQLException, ClassNotFoundException, InterruptedException, IOException {

        // serialization failures return an SQL_STATE value of '40001'
        final String CONCURRENT_UPDATE_ERROR_SQL_STATE = "40001";

        final int NUMBER_OF_MESSAGES = 5000;

        final int NUMBER_OF_CONCURRENT_READERS = 20;

        // add NUMBER_OF_CONCURRENT_READERS + 1 clients in the system
        // the + 1 is for having one more client that corresponds to the sender of all the initial messages
        // found in the system
        final int NUMBER_OF_QUEUES = 1;
        InitializeDatabase.initializeDatabaseWithClientsAndQueues(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD,
                isolationLevel, new String[]{}, NUMBER_OF_CONCURRENT_READERS + 1, NUMBER_OF_QUEUES);

        // numberOfReadMessagesByReader[i] contains the number of messages the i-th reader read
        final int[] numberOfReadMessagesByReader = new int[NUMBER_OF_CONCURRENT_READERS];

        // save the contents taken from the read messages by every concurrent reader and put it in this queue.
        // Contents of messages are unique so if we have twice the same content read there is a problem
        final BlockingQueue<String> contentsRead = new LinkedBlockingQueue<>();

        // the sender of the messages in the system
        final int senderId = NUMBER_OF_CONCURRENT_READERS + 1;

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
                try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD)) {
                    for (int i = 0; i < NUMBER_OF_MESSAGES; ++i) {

                        if (isolationLevel == Connection.TRANSACTION_REPEATABLE_READ) {
                            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                        } else {
                            assertTrue(isolationLevel == Connection.TRANSACTION_READ_COMMITTED);
                        }


                        String callToPrepare = RECEIVE_MESSAGE;
                        if (fromSender) {
                            callToPrepare = RECEIVE_MESSAGE_FROM_SENDER;
                        }

                        try (CallableStatement stmt = connection.prepareCall(callToPrepare)) {

                            boolean retrieveByArrivalTime = false;
                            stmt.setInt(1, (id + 1));

                            if (fromSender) {
                                stmt.setInt(2, senderId);
                            } else {
                                stmt.setInt(2, 1);
                            }

                            stmt.setBoolean(3, retrieveByArrivalTime);

                            ResultSet rs = null;
                            try {
                                rs = stmt.executeQuery();
                            } catch (PSQLException e) {

                                // only repeat the read if you are in REPEATABLE_READ isolation level
                                // in READ_COMMITTED there is no need to repeat the transaction
                                if (isolationLevel == Connection.TRANSACTION_REPEATABLE_READ) {
                                    String errorSQLState = e.getServerErrorMessage().getSQLState();

                                    if (errorSQLState.equals(CONCURRENT_UPDATE_ERROR_SQL_STATE)) {
                                        continue;
                                    }
                                }
                            }
                            boolean thereAreRows = rs.next();
                            if (thereAreRows) {
                                String content = rs.getString(6);
                                if (contentsRead.contains(content)) {
                                    // received back the same content!
                                    fail();
                                }

                                contentsRead.add(content);
                                numberOfReadMessagesByReader[id]++;
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
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
            totalReadMessages += numberOfReadMessagesByReader[i];
        }

        // verify that the messages read by the concurrent readers are actually the messages
        // that were in the system
        assertEquals(totalReadMessages, NUMBER_OF_MESSAGES);

        // verify that the message table is EMPTY
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD)) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM message");
            assertFalse(rs.next());
        }
    }

}
