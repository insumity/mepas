package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.utils.Utilities;
import org.postgresql.util.PSQLException;
import org.testng.annotations.*;

import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import static ch.ethz.inf.asl.utils.TestConstants.INTEGRATION;
import static org.testng.Assert.*;

// why don't I do integration tests from the protocol? e.g. createQueue and check the real db?? TODO Fixme
// So the idea would be that with the DbIntegrationTest I'm actually creating unit tests for my stored procedures
// I could have written integration tests for the MWMessagingProtocolImpl but then I wouldn't be able to test
// immediately the test procedures, e.g. what would happen if the requestingId is null .. blah blah blah

public class DbIntegrationTest {

    private static final String INITIALIZE_DATABASE = "{ call initialize_database() }"; // TODO add tests for those guys
    private static final String CREATE_CLIENT = "{ ? = call create_client(?) }"; // TODO

    private Connection getConnection(String db) throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/" + db, "bandwitch", "");
    }

    private boolean messageExists(int senderId, Integer receiverId, int queueId, Timestamp arrivalTime, String message) throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        Statement stmt = connection.createStatement();

        ResultSet rs;
        if (receiverId == null) {
            rs = stmt.executeQuery("SELECT COUNT(*) FROM message WHERE sender_id = " + senderId
                    + " AND receiver_id IS NULL AND queue_id = " + queueId + " AND  arrival_time = '"
                    + arrivalTime + "' AND message = '" + message + "'");
        }
        else {
            rs = stmt.executeQuery("SELECT COUNT(*) FROM message WHERE sender_id = " + senderId
                    + " AND receiver_id = " + receiverId + " AND queue_id = " + queueId + " AND  arrival_time = '"
                    + arrivalTime + "' AND message = '" + message + "'");

        }
        rs.next();
        int numberOfRows = rs.getInt(1);
        stmt.close();
        connection.close();
        return numberOfRows == 1;
    }

    private int numberOfRows(String table) throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
        rs.next();
        int numberOfRows = rs.getInt(1);
        stmt.close();
        connection.close();
        return numberOfRows;
    }

    private void loadSQLFile(String username, String db, String filePath) throws IOException, InterruptedException {
        final String [] cmd = { "psql",
                "-U", username,
                "-d", db,
                "-f", filePath
        };

        try {
            Process process = Runtime.getRuntime().exec(cmd);
            if (process != null) {
                if (process.waitFor() != 0) {
                    System.err.println("Abnormal termination!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // TODO .. add finally blocks here and in tearDown
    @BeforeMethod(groups = INTEGRATION)
    public void initialize() throws ClassNotFoundException, SQLException, IOException, InterruptedException {

        // create the database
        Connection connection = getConnection("");
        connection.createStatement().execute("CREATE DATABASE integrationtest");
        connection.close();


        // load all the functions from `auxiliary_function.sql` and `basic_functions.sql`
        loadSQLFile("bandwitch", "integrationtest", "resources/auxiliary_functions.sql");
        loadSQLFile("bandwitch", "integrationtest", "resources/basic_functions.sql");

        // create all the relations: queue, client and message
        connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(INITIALIZE_DATABASE);
        stmt.execute();
        stmt.close();
        connection.close();

        // populate the tables
        loadSQLFile("bandwitch", "integrationtest", "test_resources/populate_database.sql");
    }

    @AfterMethod(groups = INTEGRATION)
    public void tearDown() throws SQLException, ClassNotFoundException {
        // drop the database
        Connection connection = getConnection("");
        connection.createStatement().execute("DROP DATABASE integrationtest");
        connection.close();
    }

    // FIXME .. connection might not get closed???!!!
    // TODO ...... read those from the file or somewhere move up
    private static final int NUMBER_OF_QUEUES = 6;
    private static final int NUMBER_OF_MESSAGES = 11;

    @Test(groups = INTEGRATION)
    public void testCreateQueue() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.CREATE_QUEUE);
        stmt.registerOutParameter(1, Types.INTEGER);
        stmt.setString(2, "somename");
        stmt.execute();
        assertEquals(stmt.getInt(1), NUMBER_OF_QUEUES + 1);
        stmt.close();
        connection.close();

        int numberOfQueues = numberOfRows("queue");
        assertEquals(numberOfQueues, NUMBER_OF_QUEUES + 1);
    }

    @Test(groups = INTEGRATION)
    public void testDeleteQueue() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.DELETE_QUEUE);
        int unusedQueueId = 6; // this queue is not used in any message
        stmt.setInt(1, unusedQueueId);
        stmt.execute();
        stmt.close();
        connection.close();

        int numberOfQueues = numberOfRows("queue");
        assertEquals(numberOfQueues, NUMBER_OF_QUEUES - 1);
    }

    @Test(groups = INTEGRATION, expectedExceptions = PSQLException.class, expectedExceptionsMessageRegExp = ".* INVALID_QUEUE with \\(queue_id\\)=.*")
    public void testDeleteQueueWithNonExistentQueueId() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.DELETE_QUEUE);
        int nonExistentQueueId = 100;
        stmt.setInt(1, nonExistentQueueId);
        try {
            stmt.execute();
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION, expectedExceptions = PSQLException.class, expectedExceptionsMessageRegExp = "(?s).*ERROR: update or delete on .*")
    public void testDeleteQueueWithQueueIdUsedByAMessage() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.DELETE_QUEUE);
        int queueIdUsedByAMessage = 4;
        stmt.setInt(1, queueIdUsedByAMessage);
        try {
            stmt.execute();
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION)
    public void testSendMessage() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.SEND_MESSAGE);
        int senderId = 2;
        int receiverId = 3;
        int queueId = 5;
        Timestamp arrivalTime = Timestamp.from(Instant.now());
        String message = Utilities.createStringWith(200, 'A');
        stmt.setInt(1, senderId);
        stmt.setInt(2, receiverId);
        stmt.setInt(3, queueId);
        stmt.setTimestamp(4, arrivalTime);
        stmt.setString(5, message);

        try {
            stmt.execute();
        }
        finally {
            stmt.close();
            connection.close();
        }

        int numberOfQueues = numberOfRows("message");
        assertEquals(numberOfQueues, NUMBER_OF_MESSAGES + 1);
        boolean messageExists = messageExists(senderId, receiverId, queueId, arrivalTime, message);
        assertTrue(messageExists);
    }

    @Test(groups = INTEGRATION)
    public void testSendMessageWithNullReceiverId() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.SEND_MESSAGE);
        int senderId = 4;
        int queueId = 2;
        Timestamp arrivalTime = Timestamp.from(Instant.now());
        String message = Utilities.createStringWith(2000, 'A');
        stmt.setInt(1, senderId);
        stmt.setNull(2, Types.INTEGER);
        stmt.setInt(3, queueId);
        stmt.setTimestamp(4, arrivalTime);
        stmt.setString(5, message);

        try {
            stmt.execute();
        }
        finally {
            stmt.close();
            connection.close();
        }

        int numberOfQueues = numberOfRows("message");
        assertEquals(numberOfQueues, NUMBER_OF_MESSAGES + 1);
        boolean messageExists = messageExists(senderId, null, queueId, arrivalTime, message);
        assertTrue(messageExists);
    }

    @Test(groups = INTEGRATION, expectedExceptions = PSQLException.class, expectedExceptionsMessageRegExp = "(?s).*column \"sender_id\" violates not-null.*")
    public void testSendMessageWithNullSenderId() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.SEND_MESSAGE);
        int receiverId = 4;
        int queueId = 2;
        Timestamp arrivalTime = Timestamp.from(Instant.now());
        String message = Utilities.createStringWith(2000, 'A');
        stmt.setNull(1, Types.INTEGER);
        stmt.setInt(2, receiverId);
        stmt.setInt(3, queueId);
        stmt.setTimestamp(4, arrivalTime);
        stmt.setString(5, message);

        try {
            stmt.execute();
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION, expectedExceptions = PSQLException.class, expectedExceptionsMessageRegExp = "(?s).*column \"queue_id\" violates not-null.*")
    public void testSendMessageWithNullQueueId() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.SEND_MESSAGE);
        int senderId = 1;
        int receiverId = 4;
        Timestamp arrivalTime = Timestamp.from(Instant.now());
        String message = Utilities.createStringWith(2000, 'A');
        stmt.setInt(1, senderId);
        stmt.setInt(2, receiverId);
        stmt.setNull(3, Types.INTEGER);
        stmt.setTimestamp(4, arrivalTime);
        stmt.setString(5, message);

        try {
            stmt.execute();
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION, expectedExceptions = PSQLException.class, expectedExceptionsMessageRegExp = "(?s).*column \"arrival_time\" violates not-null.*")
    public void testSendMessageWithNullArrivalTime() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.SEND_MESSAGE);
        int senderId = 1;
        int receiverId = 4;
        int queueId = 3;
        String message = Utilities.createStringWith(2000, 'A');
        stmt.setInt(1, senderId);
        stmt.setInt(2, receiverId);
        stmt.setInt(3, queueId);
        stmt.setNull(4, Types.TIMESTAMP);
        stmt.setString(5, message);

        try {
            stmt.execute();
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION, expectedExceptions = PSQLException.class, expectedExceptionsMessageRegExp = "(?s).*column \"message\" violates not-null.*")
    public void testSendMessageWithNullMessage() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.SEND_MESSAGE);
        int senderId = 1;
        int receiverId = 4;
        int queueId = 3;
        Timestamp arrivalTime = Timestamp.from(Instant.now());
        stmt.setInt(1, senderId);
        stmt.setInt(2, receiverId);
        stmt.setInt(3, queueId);
        stmt.setTimestamp(4, arrivalTime);
        stmt.setNull(5, Types.VARCHAR);

        try {
            stmt.execute();
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION, expectedExceptions = PSQLException.class, expectedExceptionsMessageRegExp = "(?s).*violates check constraint \"check_cannot_send_to_itself\".*")
    public void testSendMessageWithSenderIdEqualToReceiverId() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.SEND_MESSAGE);
        int senderId = 2;
        int receiverId = 2;
        int queueId = 3;
        Timestamp arrivalTime = Timestamp.from(Instant.now());
        String message = Utilities.createStringWith(200, 'A');
        stmt.setInt(1, senderId);
        stmt.setInt(2, receiverId);
        stmt.setInt(3, queueId);
        stmt.setTimestamp(4, arrivalTime);
        stmt.setString(5, message);

        try {
            stmt.execute();
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    // FIXME put above
    private static final String MESSAGE_CONSTANT = "DsS1VtFXHo5ssaBpuwyZxzOUHfG5Q5jZPKBpP4r0aDynyti6SKoIjPze0iJsIJ9agBRyrUQHBXxUx2fC7qZpbLINLq6jskPoPNq32bTonqgDpk2RMqEoxOFgo4fAusBhIrCXhfrLIpYHGHfmg4E0lS2Hua86T8aKZ5L7giog0WKq7yZAEqEkoiQlkwu54OEDPuDVSyWN";

    class DbMessage {
        private int rowId;
        private int senderId;
        private Integer receiverId;
        private int queueId;
        private Timestamp arrivalTime;
        private String message;
        public DbMessage(int rowId, int senderId, Integer receiverId, int queueId, Timestamp arrivalTime, String message) {
            this.rowId = rowId;
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.queueId = queueId;
            this.arrivalTime = arrivalTime;
            this.message = message;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DbMessage) {
                DbMessage other = (DbMessage) obj;
                return (this.rowId == other.rowId) && (this.senderId == other.senderId)
                        && (this.receiverId == other.receiverId) && (this.queueId == other.queueId)
                        && (this.arrivalTime.equals(other.arrivalTime)) && (this.message.equals(other.message));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return -1; // TODO
        }

        @Override
        public String toString() {
            return "(" + rowId + ", " + senderId + ", " + receiverId + ", " + queueId + ", " + arrivalTime + ")";
        }
    }

    // FIXME put above, think about DBMessage
    public DbMessage createMessage(ResultSet rs) throws SQLException {
        int readRowId = rs.getInt(1);
        int readSenderId = rs.getInt(2);
        Integer readReceiverId = rs.getInt(3);
        if (rs.wasNull()) {
            readReceiverId = null;
        }
        int readQueueId = rs.getInt(4);
        Timestamp arrivalTime = rs.getTimestamp(5);
        String message = rs.getString(6); // should be readMessage to be consistent with the others

        return new DbMessage(readRowId, readSenderId, readReceiverId, readQueueId, arrivalTime, message);
    }

    @Test(groups = INTEGRATION)
    public void testReceiveMessage() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.RECEIVE_MESSAGE);
        int requestingUserId = 5;
        int queueId = 4;
        boolean retrieveByArrivalTime = false;
        stmt.setInt(1, requestingUserId);
        stmt.setInt(2, queueId);
        stmt.setBoolean(3, retrieveByArrivalTime);

        try {
            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();

            assertTrue(thereAreRows);

            int numberOfQueues = numberOfRows("message");
            assertEquals(numberOfQueues, NUMBER_OF_MESSAGES - 1);

            DbMessage actualMessage = createMessage(rs);
            DbMessage expectedMessage = new DbMessage(5, 4, 5, 4, Timestamp.valueOf("1999-01-08 04:05:06"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION)
    public void testReceiveMessageThatHasNullReceiverId() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.RECEIVE_MESSAGE);
        int requestingUserId = 2;
        int queueId = 3;
        boolean retrieveByArrivalTime = false;
        stmt.setInt(1, requestingUserId);
        stmt.setInt(2, queueId);
        stmt.setBoolean(3, retrieveByArrivalTime);

        try {
            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();

            assertTrue(thereAreRows);

            int numberOfQueues = numberOfRows("message");
            assertEquals(numberOfQueues, NUMBER_OF_MESSAGES - 1);

            int readRowId = rs.getInt(1);
            assertEquals(readRowId, 11);

            int readSenderId = rs.getInt(2);
            assertEquals(readSenderId, 5);

            // there is no specific receiver
            rs.getInt(3);
            assertTrue(rs.wasNull());

            int readQueueId = rs.getInt(4);
            assertEquals(readQueueId, queueId);

            Timestamp arrivalTime = rs.getTimestamp(5);
            assertEquals(arrivalTime, Timestamp.valueOf("1999-01-08 04:15:23"));

            String message = rs.getString(6);
            assertEquals(message, MESSAGE_CONSTANT);


            // FIXME doesn't work here ... because of @#$#@$ NULL
//            DbMessage actualMessage = createMessage(rs);
//            DbMessage expectedMessage = new DbMessage(11, 5, queueId, 4, Timestamp.valueOf("1999-01-08 04:05:06"), MESSAGE_CONSTANT);

//            assertEquals(actualMessage, expectedMessage);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION, expectedExceptions = PSQLException.class, expectedExceptionsMessageRegExp = "(?s).*INVALID_REQUESTING_USER_ID is NULL.*")
    public void testReceiveMessageWithNullRequestingUserId() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.RECEIVE_MESSAGE);
        int queueId = 3;
        boolean retrieveByArrivalTime = false;
        stmt.setNull(1, Types.INTEGER);
        stmt.setInt(2, queueId);
        stmt.setBoolean(3, retrieveByArrivalTime);

        try {
            stmt.executeQuery();
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION)
    public void testReceiveMessageBasedOnRetrievalTime() throws SQLException, ClassNotFoundException {

        // there are 3 messages for user 2 in queue 1
        // we are going to receive them all one after the other and make sure they are received based
        // on their arrival time
        Connection connection = getConnection("integrationtest");

        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.RECEIVE_MESSAGE);
        int requestingUserId = 2;
        int queueId = 1;
        // it cannot be easily tested with retrieveArrivalTime = false because even then messages
        // could be received in order (READ HERE: If ORDER BY is not given, the rows are returned in whatever order the system finds fastest to produce.
        // from http://www.postgresql.org/docs/9.0/static/sql-select.html)
        boolean retrieveByArrivalTime = true;
        stmt.setInt(1, requestingUserId);
        stmt.setInt(2, queueId);
        stmt.setBoolean(3, retrieveByArrivalTime);

        try {
            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();
            assertTrue(thereAreRows);

            DbMessage actualMessage = createMessage(rs);
            DbMessage expectedMessage = new DbMessage(8, 5, 2, queueId, Timestamp.valueOf("2001-01-08 04:05:06"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }
        finally {
            stmt.close();
            connection.close();
        }

        connection = getConnection("integrationtest");
        stmt = connection.prepareCall(MWMessagingProtocolImpl.RECEIVE_MESSAGE);
        stmt.setInt(1, requestingUserId);
        stmt.setInt(2, queueId);
        stmt.setBoolean(3, retrieveByArrivalTime);

        try {
            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();
            assertTrue(thereAreRows);

            int numberOfQueues = numberOfRows("message");
            assertEquals(numberOfQueues, NUMBER_OF_MESSAGES - 2);

            DbMessage actualMessage = createMessage(rs);
            DbMessage expectedMessage = new DbMessage(9, 3, 2, queueId, Timestamp.valueOf("2000-01-08 04:05:06"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }
        finally {
            stmt.close();
            connection.close();
        }


        connection = getConnection("integrationtest");
        stmt = connection.prepareCall(MWMessagingProtocolImpl.RECEIVE_MESSAGE);
        stmt.setInt(1, requestingUserId);
        stmt.setInt(2, queueId);
        stmt.setBoolean(3, retrieveByArrivalTime);

        try {
            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();
            assertTrue(thereAreRows);

            DbMessage actualMessage = createMessage(rs);
            DbMessage expectedMessage = new DbMessage(1, 1, 2, queueId, Timestamp.valueOf("1999-01-08 04:05:06"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            String message = rs.getString(6);
            assertEquals(message, MESSAGE_CONSTANT);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }
        finally {
            stmt.close();
            connection.close();
        }
    }


    @Test(groups = INTEGRATION)
    public void testReceiveMessageUserDoesNotReceiveHisOwnMessage() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");

        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.RECEIVE_MESSAGE);
        int requestingUserId = 5;
        int queueId = 3; // queue with id 3 contains this message:   "5 (sender_id)| NULL (receiver_id) | 3 (queue_id)"
        boolean retrieveByArrivalTime = false;

        stmt.setInt(1, requestingUserId);
        stmt.setInt(2, queueId);
        stmt.setBoolean(3, retrieveByArrivalTime);

        try {
            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();
            assertFalse(thereAreRows);
        }
        finally {
            stmt.close();
            connection.close();
        }

        // verify that the message in queue with id 3 can be received by some other user
        connection = getConnection("integrationtest");
        stmt = connection.prepareCall(MWMessagingProtocolImpl.RECEIVE_MESSAGE);

        requestingUserId = 4;
        stmt.setInt(1, requestingUserId);
        stmt.setInt(2, queueId);
        stmt.setBoolean(3, retrieveByArrivalTime);
        try {
            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();
            assertTrue(thereAreRows);

            DbMessage actualMessage = createMessage(rs);
            DbMessage expectedMessage = new DbMessage(11, 5, null, queueId, Timestamp.valueOf("1999-01-08 04:15:23"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            String message = rs.getString(6);
            assertEquals(message, MESSAGE_CONSTANT);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);

            int numberOfQueues = numberOfRows("message");
            assertEquals(numberOfQueues, NUMBER_OF_MESSAGES - 1);
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION)
    public void testReadMessage() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.READ_MESSAGE);

        int requestingUserId = 1;
        int queueId = 1;
        boolean retrieveByArrivalTime = false;
        stmt.setInt(1, requestingUserId);
        stmt.setInt(2, queueId);
        stmt.setBoolean(3, retrieveByArrivalTime);

        try {
            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();

            assertTrue(thereAreRows);

            int numberOfQueues = numberOfRows("message");
            assertEquals(numberOfQueues, NUMBER_OF_MESSAGES);

            DbMessage actualMessage = createMessage(rs);
            DbMessage expectedMessage = new DbMessage(7, 2, 1, 1, Timestamp.valueOf("1999-01-08 04:05:06"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION)
    public void testReadMessageThatHasNullReceiverId() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.READ_MESSAGE);

        int requestingUserId = 4;
        int queueId = 3;
        boolean retrieveByArrivalTime = false;
        stmt.setInt(1, requestingUserId);
        stmt.setInt(2, queueId);
        stmt.setBoolean(3, retrieveByArrivalTime);

        try {
            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();

            assertTrue(thereAreRows);

            int numberOfQueues = numberOfRows("message");
            assertEquals(numberOfQueues, NUMBER_OF_MESSAGES);

            DbMessage actualMessage = createMessage(rs);
            DbMessage expectedMessage = new DbMessage(11, 5, null, 3, Timestamp.valueOf("1999-01-08 04:15:23"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION)
    public void testReadMessageUserDoesNotReadHisOwnMessage() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.READ_MESSAGE);

        int requestingUserId = 5;
        int queueId = 3;
        boolean retrieveByArrivalTime = false;
        stmt.setInt(1, requestingUserId);
        stmt.setInt(2, queueId);
        stmt.setBoolean(3, retrieveByArrivalTime);

        try {
            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();

            assertFalse(thereAreRows);
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION, expectedExceptions = PSQLException.class, expectedExceptionsMessageRegExp = "(?s).*INVALID_REQUESTING_USER_ID is NULL.*")
    public void testReadMessageWithNullRequestingUserId() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.READ_MESSAGE);
        int queueId = 3;
        boolean retrieveByArrivalTime = false;
        stmt.setNull(1, Types.INTEGER);
        stmt.setInt(2, queueId);
        stmt.setBoolean(3, retrieveByArrivalTime);

        try {
            stmt.executeQuery();
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION)
    public void testReceiveMessageFromSender() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.RECEIVE_MESSAGE_FROM_SENDER);

        int requestingUserId = 5;
        int senderId = 4;
        boolean retrieveByArrivalTime = false;
        stmt.setInt(1, requestingUserId);
        stmt.setInt(2, senderId);
        stmt.setBoolean(3, retrieveByArrivalTime);

        try {
            ResultSet rs = stmt.executeQuery();
            boolean thereAreRows = rs.next();

            assertTrue(thereAreRows);

            int numberOfQueues = numberOfRows("message");
            assertEquals(numberOfQueues, NUMBER_OF_MESSAGES - 1);

            DbMessage actualMessage = createMessage(rs);
            DbMessage expectedMessage = new DbMessage(5, senderId, requestingUserId, 4, Timestamp.valueOf("1999-01-08 04:05:06"), MESSAGE_CONSTANT);

            assertEquals(actualMessage, expectedMessage);

            boolean hasMoreRows = rs.next();
            assertFalse(hasMoreRows);
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION, expectedExceptions = PSQLException.class, expectedExceptionsMessageRegExp = "(?s).*INVALID_REQUESTING_USER_ID is NULL.*")
    public void testReceiveMessageFromSenderWithNullRequestingUserId() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.RECEIVE_MESSAGE_FROM_SENDER);

        int senderId = 4;
        boolean retrieveByArrivalTime = false;
        stmt.setNull(1, Types.INTEGER);
        stmt.setInt(2, senderId);
        stmt.setBoolean(3, retrieveByArrivalTime);

        try {
            stmt.executeQuery();
        }
        finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION, expectedExceptions = PSQLException.class, expectedExceptionsMessageRegExp = "(?s).*INVALID_SENDER_ID with same id as the one issuing.*")
    public void testReceiveMessageFromSenderWithRequestingUserIdEqualToSenderId() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.RECEIVE_MESSAGE_FROM_SENDER);

        int requestingUserId = 3;
        int senderId = 3;
        boolean retrieveByArrivalTime = false;
        stmt.setInt(1, requestingUserId);
        stmt.setInt(2, senderId);
        stmt.setBoolean(3, retrieveByArrivalTime);

        try {
            stmt.executeQuery();
        } finally {
            stmt.close();
            connection.close();
        }
    }

    @Test(groups = INTEGRATION)
    public void testListQueues() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        // TODO make all MWMe..Protocol static imports
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.LIST_QUEUES);

        int requestingUserId = 5;
        stmt.setInt(1, requestingUserId);

        try {
            ResultSet rs = stmt.executeQuery();
            List<Integer> queues = new LinkedList<Integer>();

            while (rs.next()) {
                queues.add(rs.getInt(1));
            }

            Integer[] expectedQueues = new Integer[] {2, 4};
            Integer[] actualQueues = queues.toArray(new Integer[0]);

            assertEqualsNoOrder(actualQueues, expectedQueues);
        } finally {
            stmt.close();
            connection.close();

        }
    }

    @Test(groups = INTEGRATION, expectedExceptions = PSQLException.class, expectedExceptionsMessageRegExp = "(?s).*INVALID_REQUESTING_USER_ID is NULL.*")
    public void testListQueuesWithNullRequestingUserId() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        // TODO make all MWMe..Protocol static imports
        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.LIST_QUEUES);

        stmt.setNull(1, Types.INTEGER);

        try {
            stmt.executeQuery();
        } finally {
            stmt.close();
            connection.close();

        }
    }
}