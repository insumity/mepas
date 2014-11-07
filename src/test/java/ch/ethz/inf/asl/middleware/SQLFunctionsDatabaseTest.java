package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.testutils.InitializeDatabase;
import ch.ethz.inf.asl.testutils.Utilities;
import org.postgresql.util.PSQLException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static ch.ethz.inf.asl.common.MessageConstants.MAXIMUM_MESSAGE_LENGTH;
import static ch.ethz.inf.asl.middleware.MiddlewareMessagingProtocolImpl.*;
import static ch.ethz.inf.asl.testutils.InitializeDatabase.getConnection;
import static ch.ethz.inf.asl.testutils.TestConstants.*;
import static org.testng.Assert.*;

/**
 * Tests for the stored procedures (functions) of the basic functionality of the system. Those tests use
 * an actual database which is initially populated and then functions are called and the returned results
 * are verified.
 */
public class SQLFunctionsDatabaseTest {


    // initial number of queues and messages contained in the corresponding tables after populating them
    private static int NUMBER_OF_CLIENTS = 6;
    private static int NUMBER_OF_QUEUES = 6;
    private static final int NUMBER_OF_MESSAGES = 11;

    // message content as the one that exists in all the messages after populating the database
    private static final String MESSAGE_CONSTANT = "DsS1VtFXHo5ssaBpuwyZxzOUHfG5Q5jZPKBpP4r0aDynyti6SKoIjPze" +
            "0iJsIJ9agBRyrUQHBXxUx2fC7qZpbLINLq6jskPoPNq32bTonqgDpk2RMqEoxOFgo4fAusBhIrCXhfrLIpYHGHfmg4E0lS2" +
            "Hua86T8aKZ5L7giog0WKq7yZAEqEkoiQlkwu54OEDPuDVSyWN";

    /* checks if given message exists in the database, if so returns true, otherwise false */
    private boolean messageExists(int senderId, Integer receiverId, int queueId, Timestamp arrivalTime, String message)
            throws SQLException, ClassNotFoundException {

        String command;
        if (receiverId == null) { // no receiver
            command = "SELECT COUNT(*) FROM message WHERE sender_id = " + senderId
                    + " AND receiver_id IS NULL AND queue_id = " + queueId + " AND  arrival_time = '"
                    + arrivalTime + "' AND message = '" + message + "'";
        } else {
            command = "SELECT COUNT(*) FROM message WHERE sender_id = " + senderId
                    + " AND receiver_id = " + receiverId + " AND queue_id = " + queueId + " AND  arrival_time = '"
                    + arrivalTime + "' AND message = '" + message + "'";
        }

        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(command)) {

            rs.next();
            int numberOfRows = rs.getInt(1);
            return numberOfRows == 1;
        }
    }

    /* returns the number of rows the given table has in the given database */
    private int numberOfRows(String database, String table) throws SQLException, ClassNotFoundException {

        try (Connection connection = getConnection(HOST, PORT_NUMBER, database, USERNAME, PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {

            rs.next();
            int numberOfRows = rs.getInt(1);
            return numberOfRows;
        }
    }

    @BeforeMethod(groups = DATABASE)
    public void initialize() throws ClassNotFoundException, SQLException, IOException, InterruptedException {
        String populateDatabaseCode = "src/test/resources/populate_database.sql";
        InitializeDatabase.initializeDatabase(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD, new String[]{populateDatabaseCode});
    }

    @Test(groups = DATABASE)
    public void testCreateClient() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(CREATE_CLIENT)) {

            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setString(2, "someclient");
            stmt.execute();
            assertEquals(stmt.getInt(1), NUMBER_OF_CLIENTS + 1);
        }

        int numberOfClients = numberOfRows(DATABASE_NAME, "client");
        assertEquals(numberOfClients, NUMBER_OF_CLIENTS + 1);
    }

    @Test(groups = DATABASE)
    public void testDeleteClient() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(DELETE_CLIENT)) {

            int unusedClientId = 6; // this client is not used in any message
            stmt.setInt(1, unusedClientId);
            stmt.execute();
        }

        int numberOfClients = numberOfRows(DATABASE_NAME, "client");
        assertEquals(numberOfClients, NUMBER_OF_CLIENTS - 1);
    }

    @Test(groups = DATABASE, expectedExceptions = PSQLException.class,
            expectedExceptionsMessageRegExp = ".*DELETE_CLIENT: trying to delete a non existent client.*")
    public void testDeleteClientWithNonExistentClientId() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(DELETE_CLIENT)) {

            int nonExistentClientId = 100;
            stmt.setInt(1, nonExistentClientId);
            stmt.execute();
        }
    }

    @Test(groups = DATABASE, expectedExceptions = PSQLException.class,
            expectedExceptionsMessageRegExp = "(?s).*ERROR: update or delete on .*")
    public void testDeleteClientWithQueueIdUsedByAMessage() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(DELETE_CLIENT)) {

            int clientIdUsedByAMessage = 4;
            stmt.setInt(1, clientIdUsedByAMessage);
            stmt.execute();
        }
    }

    @Test(groups = DATABASE)
    public void testCreateQueue() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(CREATE_QUEUE)) {

            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setString(2, "somename");
            stmt.execute();
            assertEquals(stmt.getInt(1), NUMBER_OF_QUEUES + 1);
        }

        int numberOfQueues = numberOfRows(DATABASE_NAME, "queue");
        assertEquals(numberOfQueues, NUMBER_OF_QUEUES + 1);
    }

    @Test(groups = DATABASE)
    public void testDeleteQueue() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(DELETE_QUEUE)) {

            int unusedQueueId = 6; // this queue is not used in any message
            stmt.setInt(1, unusedQueueId);
            stmt.execute();
        }

        int numberOfQueues = numberOfRows(DATABASE_NAME, "queue");
        assertEquals(numberOfQueues, NUMBER_OF_QUEUES - 1);
    }

    @Test(groups = DATABASE, expectedExceptions = PSQLException.class,
            expectedExceptionsMessageRegExp = ".*DELETE_QUEUE: trying to delete a non existent queue.*")
    public void testDeleteQueueWithNonExistentQueueId() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(DELETE_QUEUE)) {

            int nonExistentQueueId = 100;
            stmt.setInt(1, nonExistentQueueId);
            stmt.execute();
        }
    }

    @Test(groups = DATABASE, expectedExceptions = PSQLException.class,
            expectedExceptionsMessageRegExp = "(?s).*ERROR: update or delete on .*")
    public void testDeleteQueueWithQueueIdUsedByAMessage() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(DELETE_QUEUE)) {

            int queueIdUsedByAMessage = 4;
            stmt.setInt(1, queueIdUsedByAMessage);
            stmt.execute();
        }
    }

    @Test(groups = DATABASE)
    public void testSendMessage() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(SEND_MESSAGE)) {

            int senderId = 2;
            int receiverId = 3;
            int queueId = 5;
            Timestamp arrivalTime = Timestamp.valueOf("2014-12-12 12:34:12");
            String message = Utilities.createStringWith(20, 'A');
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, queueId);
            stmt.setTimestamp(4, arrivalTime);
            stmt.setString(5, message);
            stmt.execute();

            boolean messageExists = messageExists(senderId, receiverId, queueId, arrivalTime, message);
            assertTrue(messageExists);
        }

        int numberOfQueues = numberOfRows(DATABASE_NAME, "message");
        assertEquals(numberOfQueues, NUMBER_OF_MESSAGES + 1);

    }

    @Test(groups = DATABASE)
    public void testSendMessageWithNullReceiverId() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(SEND_MESSAGE)) {

            int senderId = 4;
            int queueId = 2;
            Timestamp arrivalTime = Timestamp.valueOf("2014-12-12 12:34:12");
            String message = Utilities.createStringWith(20, 'A');
            stmt.setInt(1, senderId);
            stmt.setNull(2, Types.INTEGER);
            stmt.setInt(3, queueId);
            stmt.setTimestamp(4, arrivalTime);
            stmt.setString(5, message);
            stmt.execute();

            boolean messageExists = messageExists(senderId, null, queueId, arrivalTime, message);
            assertTrue(messageExists);
        }

        int numberOfQueues = numberOfRows(DATABASE_NAME, "message");
        assertEquals(numberOfQueues, NUMBER_OF_MESSAGES + 1);

    }

    @Test(groups = DATABASE, expectedExceptions = PSQLException.class,
            expectedExceptionsMessageRegExp = "(?s).*column \"sender_id\" violates not-null.*")
    public void testSendMessageWithNullSenderId() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(SEND_MESSAGE)) {

            int receiverId = 4;
            int queueId = 2;
            Timestamp arrivalTime = Timestamp.valueOf("2014-12-12 12:34:12");
            String message = Utilities.createStringWith(20, 'A');
            stmt.setNull(1, Types.INTEGER);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, queueId);
            stmt.setTimestamp(4, arrivalTime);
            stmt.setString(5, message);
            stmt.execute();
        }
    }

    @Test(groups = DATABASE, expectedExceptions = PSQLException.class,
            expectedExceptionsMessageRegExp = "(?s).*column \"queue_id\" violates not-null.*")
    public void testSendMessageWithNullQueueId() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(SEND_MESSAGE)) {

            int senderId = 1;
            int receiverId = 4;
            Timestamp arrivalTime = Timestamp.valueOf("2014-12-12 12:34:12");
            String message = Utilities.createStringWith(20, 'A');
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setNull(3, Types.INTEGER);
            stmt.setTimestamp(4, arrivalTime);
            stmt.setString(5, message);
            stmt.execute();
        }
    }

    @Test(groups = DATABASE, expectedExceptions = PSQLException.class,
            expectedExceptionsMessageRegExp = "(?s).*column \"arrival_time\" violates not-null.*")
    public void testSendMessageWithNullArrivalTime() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(SEND_MESSAGE)) {

            int senderId = 1;
            int receiverId = 4;
            int queueId = 3;
            String message = Utilities.createStringWith(20, 'A');
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, queueId);
            stmt.setNull(4, Types.TIMESTAMP);
            stmt.setString(5, message);
            stmt.execute();
        }
    }

    @Test(groups = DATABASE, expectedExceptions = PSQLException.class,
            expectedExceptionsMessageRegExp = "(?s).*column \"message\" violates not-null.*")
    public void testSendMessageWithNullMessage() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(SEND_MESSAGE)) {
            int senderId = 1;
            int receiverId = 4;
            int queueId = 3;
            Timestamp arrivalTime = Timestamp.valueOf("2014-12-12 12:34:12");
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, queueId);
            stmt.setTimestamp(4, arrivalTime);
            stmt.setNull(5, Types.VARCHAR);
            stmt.execute();
        }
    }

    @Test(groups = DATABASE, expectedExceptions = PSQLException.class,
            expectedExceptionsMessageRegExp = "(?s).*violates check constraint \"check_cannot_send_to_itself\".*")
    public void testSendMessageWithSenderIdEqualToReceiverId() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(SEND_MESSAGE)) {

            int senderId = 2;
            int receiverId = 2;
            int queueId = 3;
            Timestamp arrivalTime = Timestamp.valueOf("2014-12-12 12:34:12");
            String message = Utilities.createStringWith(20, 'A');
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, queueId);
            stmt.setTimestamp(4, arrivalTime);
            stmt.setString(5, message);
            stmt.execute();
        }
    }


    @Test(groups = DATABASE)
    public void testSendMessageWithEmptyContent() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(SEND_MESSAGE)) {

            int senderId = 2;
            int receiverId = 5;
            int queueId = 3;
            Timestamp arrivalTime = Timestamp.valueOf("2014-12-12 12:34:12");
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, queueId);
            stmt.setTimestamp(4, arrivalTime);
            stmt.setString(5, "");
            stmt.execute();
        }
    }

    /* corresponds to a message retrieved from the database */
    private class DatabaseMessage {
        private int rowId;
        private int senderId;
        private Integer receiverId;
        private int queueId;
        private Timestamp arrivalTime;
        private String message;

        public DatabaseMessage(int rowId, int senderId, Integer receiverId, int queueId, Timestamp arrivalTime, String message) {
            this.rowId = rowId;
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.queueId = queueId;
            this.arrivalTime = arrivalTime;
            this.message = message;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DatabaseMessage) {
                DatabaseMessage other = (DatabaseMessage) obj;

                boolean sameReceivers = false;

                if (this.receiverId == null) {
                    sameReceivers = (other.receiverId == null);
                }
                else {
                    sameReceivers = this.receiverId.equals(other.receiverId);
                }

                return (this.rowId == other.rowId) && (this.senderId == other.senderId)
                        && sameReceivers && (this.queueId == other.queueId)
                        && (this.arrivalTime.equals(other.arrivalTime)) && (this.message.equals(other.message));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(rowId, senderId, receiverId, queueId, arrivalTime, message);
        }

        @Override
        public String toString() {
            return "(" + rowId + ", " + senderId + ", " + receiverId + ", " + queueId + ", " + arrivalTime + ")";
        }
    }

    /* creates a db message based on the given result set */
    public DatabaseMessage createMessage(ResultSet rs) throws SQLException {
        int readRowId = rs.getInt(1);
        int readSenderId = rs.getInt(2);

        Integer readReceiverId = rs.getInt(3);
        if (rs.wasNull()) {
            readReceiverId = null;
        }

        int readQueueId = rs.getInt(4);
        Timestamp readArrivalTime = rs.getTimestamp(5);
        String readMessage = rs.getString(6);

        return new DatabaseMessage(readRowId, readSenderId, readReceiverId, readQueueId, readArrivalTime, readMessage);
    }

    @Test(groups = DATABASE)
    public void testReceiveMessage() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(RECEIVE_MESSAGE)) {

            int requestingUserId = 5;
            int queueId = 4;
            boolean retrieveByArrivalTime = false;
            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);

            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();

            assertTrue(thereAreRows);

            int numberOfQueues = numberOfRows(DATABASE_NAME, "message");
            assertEquals(numberOfQueues, NUMBER_OF_MESSAGES - 1);

            DatabaseMessage actualMessage = createMessage(rs);
            DatabaseMessage expectedMessage = new DatabaseMessage(5, 4, 5, 4, Timestamp.valueOf("1999-01-08 04:05:06"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }
    }

    @Test(groups = DATABASE)
    public void testReceiveMessageThatHasNullReceiverId() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(RECEIVE_MESSAGE)) {

            int requestingUserId = 2;
            int queueId = 3;
            boolean retrieveByArrivalTime = false;
            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);

            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();

            assertTrue(thereAreRows);

            int numberOfMessages = numberOfRows(DATABASE_NAME, "message");
            assertEquals(numberOfMessages, NUMBER_OF_MESSAGES - 1);

            DatabaseMessage actualMessage = createMessage(rs);
            DatabaseMessage expectedMessage = new DatabaseMessage(11, 5, null, 3, Timestamp.valueOf("1999-01-08 04:15:23"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }
    }

    @Test(groups = DATABASE, expectedExceptions = PSQLException.class,
            expectedExceptionsMessageRegExp = "(?s).*RECEIVE_MESSAGE: .* with p_requesting_user_id being NULL.*")
    public void testReceiveMessageWithNullRequestingUserId() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(RECEIVE_MESSAGE)) {

            int queueId = 3;
            boolean retrieveByArrivalTime = false;
            stmt.setNull(1, Types.INTEGER);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);
            stmt.executeQuery();
        }
    }

    @Test(groups = DATABASE)
    public void testReceiveMessageBasedOnRetrievalTime() throws SQLException, ClassNotFoundException {

        int requestingUserId = 2;
        int queueId = 1;
        // it cannot be easily tested with retrieveArrivalTime = false because even then messages
        // could be received in order (READ HERE: If ORDER BY is not given, the rows are returned in
        // whatever order the system finds fastest to produce.
        // from http://www.postgresql.org/docs/9.0/static/sql-select.html)
        boolean retrieveByArrivalTime = true;

        // there are 3 messages for user 2 in queue 1
        // we are going to receive them all one after the other and make sure they are received based
        // on their arrival time
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(RECEIVE_MESSAGE)) {

            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);

            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();
            assertTrue(thereAreRows);

            DatabaseMessage actualMessage = createMessage(rs);
            DatabaseMessage expectedMessage = new DatabaseMessage(1, 1, 2, queueId, Timestamp.valueOf("1999-01-08 04:05:06"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }

        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(RECEIVE_MESSAGE)) {

            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);

            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();
            assertTrue(thereAreRows);

            int numberOfQueues = numberOfRows(DATABASE_NAME, "message");
            assertEquals(numberOfQueues, NUMBER_OF_MESSAGES - 2);

            DatabaseMessage actualMessage = createMessage(rs);
            DatabaseMessage expectedMessage = new DatabaseMessage(9, 3, 2, queueId, Timestamp.valueOf("2000-01-08 04:05:06"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }


        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(RECEIVE_MESSAGE)) {

            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);

            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();
            assertTrue(thereAreRows);

            DatabaseMessage actualMessage = createMessage(rs);
            DatabaseMessage expectedMessage = new DatabaseMessage(8, 5, 2, queueId, Timestamp.valueOf("2001-01-08 04:05:06"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            String message = rs.getString(6);
            assertEquals(message, MESSAGE_CONSTANT);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }
    }


    @Test(groups = DATABASE)
    public void testReceiveMessageUserDoesNotReceiveHisOwnMessage() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
            CallableStatement stmt = connection.prepareCall(RECEIVE_MESSAGE)) {

            int requestingUserId = 5;
            int queueId = 3; // queue with id 3 contains message: "5 (sender_id)| NULL (receiver_id) | 3 (queue_id)"
            boolean retrieveByArrivalTime = false;

            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);

            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();
            assertFalse(thereAreRows);
        }

        // verify that the message in queue with id 3 can be received by some other user
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(RECEIVE_MESSAGE)) {

            int requestingUserId = 4;
            int queueId = 3; // queue with id 3 contains message: "5 (sender_id)| NULL (receiver_id) | 3 (queue_id)"
            boolean retrieveByArrivalTime = false;

            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);
            ResultSet rs = stmt.executeQuery();

            boolean thereAreRows = rs.next();
            assertTrue(thereAreRows);

            DatabaseMessage actualMessage = createMessage(rs);
            DatabaseMessage expectedMessage = new DatabaseMessage(11, 5, null, queueId, Timestamp.valueOf("1999-01-08 04:15:23"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            String message = rs.getString(6);
            assertEquals(message, MESSAGE_CONSTANT);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);

            int numberOfQueues = numberOfRows(DATABASE_NAME, "message");
            assertEquals(numberOfQueues, NUMBER_OF_MESSAGES - 1);
        }
    }

    @Test(groups = DATABASE)
    public void testReadMessage() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(READ_MESSAGE)) {

            int requestingUserId = 1;
            int queueId = 1;
            boolean retrieveByArrivalTime = false;
            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);

            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();

            assertTrue(thereAreRows);

            int numberOfQueues = numberOfRows(DATABASE_NAME, "message");
            assertEquals(numberOfQueues, NUMBER_OF_MESSAGES);

            DatabaseMessage actualMessage = createMessage(rs);
            DatabaseMessage expectedMessage = new DatabaseMessage(7, 2, 1, 1, Timestamp.valueOf("1999-01-08 04:05:06"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }
    }

    @Test(groups = DATABASE)
    public void testReadMessageThatHasNullReceiverId() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(READ_MESSAGE)) {

            int requestingUserId = 4;
            int queueId = 3;
            boolean retrieveByArrivalTime = false;
            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);

            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();

            assertTrue(thereAreRows);

            int numberOfQueues = numberOfRows(DATABASE_NAME, "message");
            assertEquals(numberOfQueues, NUMBER_OF_MESSAGES);

            DatabaseMessage actualMessage = createMessage(rs);
            DatabaseMessage expectedMessage = new DatabaseMessage(11, 5, null, 3, Timestamp.valueOf("1999-01-08 04:15:23"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }
    }

    @Test(groups = DATABASE)
    public void testReadMessageUserDoesNotReadHisOwnMessage() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(READ_MESSAGE)) {

            int requestingUserId = 5;
            int queueId = 3;
            boolean retrieveByArrivalTime = false;
            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);

            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();

            assertFalse(thereAreRows);
        }
    }

    @Test(groups = DATABASE, expectedExceptions = PSQLException.class,
            expectedExceptionsMessageRegExp = "(?s).*READ_MESSAGE: .* with p_requesting_user_id being NULL.*")
    public void testReadMessageWithNullRequestingUserId() throws SQLException, ClassNotFoundException {

        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(READ_MESSAGE)) {

            int queueId = 3;
            boolean retrieveByArrivalTime = false;
            stmt.setNull(1, Types.INTEGER);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);
            stmt.executeQuery();
        }
    }

    @Test(groups = DATABASE)
    public void testReceiveMessageFromSender() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(RECEIVE_MESSAGE_FROM_SENDER)) {

            int requestingUserId = 5;
            int senderId = 4;
            boolean retrieveByArrivalTime = false;
            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, senderId);
            stmt.setBoolean(3, retrieveByArrivalTime);

            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();

            assertTrue(thereAreRows);

            int numberOfQueues = numberOfRows(DATABASE_NAME, "message");
            assertEquals(numberOfQueues, NUMBER_OF_MESSAGES - 1);

            DatabaseMessage actualMessage = createMessage(rs);
            DatabaseMessage expectedMessage = new DatabaseMessage(5, senderId, requestingUserId, 4, Timestamp.valueOf("1999-01-08 04:05:06"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }
    }

    @Test(groups = DATABASE, expectedExceptions = PSQLException.class,
            expectedExceptionsMessageRegExp = "(?s).*RECEIVE_MESSAGE_FROM_SENDER: .* with p_requesting_user_id being NULL.*")
    public void testReceiveMessageFromSenderWithNullRequestingUserId() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(RECEIVE_MESSAGE_FROM_SENDER)) {

            int senderId = 4;
            boolean retrieveByArrivalTime = false;
            stmt.setNull(1, Types.INTEGER);
            stmt.setInt(2, senderId);
            stmt.setBoolean(3, retrieveByArrivalTime);
            stmt.executeQuery();
        }
    }

    @Test(groups = DATABASE, expectedExceptions = PSQLException.class,
            expectedExceptionsMessageRegExp = "(?s).*RECEIVE_MESSAGE_FROM_SENDER: sender id cannot be the same.*")
    public void testReceiveMessageFromSenderWithRequestingUserIdEqualToSenderId() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(RECEIVE_MESSAGE_FROM_SENDER)) {

            int requestingUserId = 3;
            int senderId = 3;
            boolean retrieveByArrivalTime = false;
            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, senderId);
            stmt.setBoolean(3, retrieveByArrivalTime);
            stmt.executeQuery();
        }
    }

    @Test(groups = DATABASE)
    public void testListQueues() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(LIST_QUEUES)) {

            int requestingUserId = 5;
            stmt.setInt(1, requestingUserId);

            ResultSet rs = stmt.executeQuery();
            List<Integer> queues = new LinkedList<>();

            while (rs.next()) {
                queues.add(rs.getInt(1));
            }

            Integer[] expectedQueues = new Integer[] {2, 4};
            Integer[] actualQueues = queues.toArray(new Integer[0]);

            assertEqualsNoOrder(actualQueues, expectedQueues);
        }
    }

    @Test(groups = DATABASE, expectedExceptions = PSQLException.class,
            expectedExceptionsMessageRegExp = "(?s).*LIST_QUEUES: .* with p_requesting_user_id being NULL.*")
    public void testListQueuesWithNullRequestingUserId() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD);
             CallableStatement stmt = connection.prepareCall(LIST_QUEUES)) {

            stmt.setNull(1, Types.INTEGER);
            stmt.executeQuery();
        }
    }
}