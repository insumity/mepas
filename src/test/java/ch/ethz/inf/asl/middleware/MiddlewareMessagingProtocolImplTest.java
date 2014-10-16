package ch.ethz.inf.asl.middleware;

import org.testng.annotations.BeforeMethod;

import java.sql.*;

import static ch.ethz.inf.asl.middleware.MiddlewareMessagingProtocolImpl.SEND_MESSAGE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MiddlewareMessagingProtocolImplTest {

    private Connection mockedConnection;
    private CallableStatement mockedStatement;

    @BeforeMethod(groups = SMALL)
    public void setUp() throws SQLException {
        mockedConnection = mock(Connection.class);
        mockedStatement = mock(CallableStatement.class);
        when(mockedConnection.prepareCall(anyString())).thenReturn(mockedStatement);
    }

//
//    @Test(groups = SMALL)
//    public void testCreateQueueCallsStoredProcedure() throws SQLException {
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(2, mockedConnection);
//        protocol.createQueue("someQueueName");
//        verify(mockedConnection).prepareCall(CREATE_QUEUE);
//        verify(mockedStatement).execute();
//    }
//
//    @Test(groups = SMALL)
//    public void testCreateQueueRetrievesResult() throws SQLException {
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(2, mockedConnection);
//        protocol.createQueue("someOtherQueueName");
//        verify(mockedStatement).getInt(1);
//    }
//
//    @Test(groups = SMALL, expectedExceptions = MessageProtocolException.class, expectedExceptionsMessageRegExp = ".*create queue.*")
//    public void testCreateQueueThrowsException() throws SQLException {
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(2, mockedConnection);
//        when(mockedStatement.execute()).thenThrow(SQLException.class);
//        protocol.createQueue("queueName");
//    }
//
//    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*length.*")
//    public void testCreateQueueWithLongQueueNameThrowsException() throws SQLException {
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(2, mockedConnection);
//        protocol.createQueue("queueNamequeueNamequeueNamequeueNamequeueName");
//    }
//
//    @Test(groups = SMALL, expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*cannot be null.*")
//    public void testCreateQueueWithNullName() throws SQLException {
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(2, mockedConnection);
//        protocol.createQueue(null);
//    }
//
//    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty.*")
//    public void testCreateQueueWithEmptyName() throws SQLException {
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(2, mockedConnection);
//        protocol.createQueue("");
//    }
//
//    @Test(groups = SMALL)
//    public void testDeleteQueueCallsStoredProcedure() throws SQLException {
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(2, mockedConnection);
//
//        int queueId = 3244;
//        protocol.deleteQueue(queueId);
//        verify(mockedConnection).prepareCall(DELETE_QUEUE);
//        verify(mockedStatement).setInt(1, queueId);
//        verify(mockedStatement).execute();
//    }
//
//
//    // FIXME perhaps rename throwsException to ... ThrowsRelevantException or something
//    @Test(groups = SMALL, expectedExceptions = MessageProtocolException.class, expectedExceptionsMessageRegExp = ".*delete queue.*")
//    public void testDeleteQueueThrowsException() throws SQLException {
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(2, mockedConnection);
//        when(mockedStatement.execute()).thenThrow(SQLException.class);
//        protocol.deleteQueue(933);
//    }

    private void verifySendMessageCall(CallableStatement mockedStatement, int requestingUserId, Integer receiverId,
                                       int queueId, String content) throws SQLException {
        verify(mockedConnection).prepareCall(SEND_MESSAGE);
        verify(mockedStatement).setInt(1, requestingUserId);

        if (receiverId == null) {
            verify(mockedStatement).setNull(2, Types.INTEGER);
        }
        else {
            verify(mockedStatement).setInt(2, receiverId);
        }
        verify(mockedStatement).setInt(3, queueId);
        verify(mockedStatement).setTimestamp(eq(4), any(Timestamp.class));
        verify(mockedStatement).setString(5, content);
        verify(mockedStatement).execute();
    }
//
//
//    @Test(groups = SMALL)
//    public void testSendMessageWithNoReceiverCallsStoredProcedure() throws SQLException {
//        int requestingUserId = 34;
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(requestingUserId, mockedConnection);
//
//        int queueId = 56;
//        String content = Utilities.createStringWith(200, 'A');
//        protocol.sendMessage(queueId, content);
//
//        verifySendMessageCall(mockedStatement, requestingUserId, null, queueId, content);
//    }
//
//    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*invalid length.*")
//    public void testSendMessageWithNoReceiverContentOfInvalidLength() throws SQLException {
//        int requestingUserId = 34;
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(requestingUserId, mockedConnection);
//
//        int queueId = 56;
//        String content = "some content, obviously with much less characters than needed";
//        protocol.sendMessage(queueId, content);
//
//        verifySendMessageCall(mockedStatement, requestingUserId, null, queueId, content);
//    }
//
//    @Test(groups = SMALL, expectedExceptions = MessageProtocolException.class, expectedExceptionsMessageRegExp = ".*send message*")
//    public void testSendMessageWithNoReceiverThrowsException() throws SQLException {
//        int requestingUserId = 34;
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(requestingUserId, mockedConnection);
//
//        int queueId = 56;
//        String content = Utilities.createStringWith(2000, 'b');
//
//        doThrow(SQLException.class).when(mockedStatement).setNull(2, Types.INTEGER);
//        protocol.sendMessage(queueId, content);
//
//        verifySendMessageCall(mockedStatement, requestingUserId, null, queueId, content);
//    }
//
//    @Test(groups = SMALL)
//    public void testSendMessageCallsStoredProcedure() throws SQLException {
//        int requestingUserId = 34;
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(requestingUserId, mockedConnection);
//
//        int receiverId = 234234;
//        int queueId = 56;
//        String content = Utilities.createStringWith(200, '@');
//        protocol.sendMessage(receiverId, queueId, content);
//
//        verifySendMessageCall(mockedStatement, requestingUserId, receiverId, queueId, content);
//    }
//
//    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*invalid length.*")
//    public void testSendMessageWithContentOfInvalidLength() throws SQLException {
//        int requestingUserId = 34;
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(requestingUserId, mockedConnection);
//
//        String content = "some content, obviously with much less characters than needed";
//        protocol.sendMessage(8324, 234, content);
//    }
//
//    @Test(groups = SMALL, expectedExceptions = MessageProtocolException.class, expectedExceptionsMessageRegExp = ".*send message.*")
//    public void testSendMessageThrowsException() throws SQLException {
//        int requestingUserId = 34;
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(requestingUserId, mockedConnection);
//
//        int receiverId = 2342341;
//        int queueId = 56;
//        String content = Utilities.createStringWith(2000, 'd');
//
//        doThrow(SQLException.class).when(mockedStatement).setInt(3, queueId);
//        protocol.sendMessage(receiverId, queueId, content);
//
//        verifySendMessageCall(mockedStatement, requestingUserId, receiverId, queueId, content);
//    }
//
//    @Test(groups = SMALL)
//    public void testReceiveMessage() throws SQLException {
//        int requestingUserId = 34;
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(requestingUserId, mockedConnection);
//
//        int queueId = 56;
//        boolean retrieveByArrivalTime = false;
//        String content = Utilities.createStringWith(2000, '%');
//        Message expectedMessage = new Message(234, requestingUserId, queueId, Timestamp.valueOf("1999-01-08 04:05:06"), content);
//
//        ResultSet mockedResultSet = mock(ResultSet.class);
//        when(mockedResultSet.next()).thenReturn(true).thenReturn(false); // only returns one row
//        when(mockedResultSet.getInt(1)).thenReturn(5);
//        when(mockedResultSet.getInt(2)).thenReturn(234);
//        when(mockedResultSet.getInt(3)).thenReturn(requestingUserId);
//        when(mockedResultSet.getInt(4)).thenReturn(queueId);
//        when(mockedResultSet.getTimestamp(5)).thenReturn(Timestamp.valueOf("1999-01-08 04:05:06"));
//        when(mockedResultSet.getString(6)).thenReturn(content);
//        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);
//
//        Optional<Message> actualMessage = protocol.receiveMessage(queueId, retrieveByArrivalTime);
//        assertTrue(actualMessage.isPresent());
//        assertEquals(actualMessage.get(), expectedMessage);
//    }
//
//    @Test(groups = SMALL)
//    public void testReceiveMessageWithNoMessage() throws SQLException {
//        int requestingUserId = 34;
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(requestingUserId, mockedConnection);
//
//        ResultSet mockedResultSet = mock(ResultSet.class);
//        when(mockedResultSet.next()).thenReturn(false);
//        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);
//
//        int queueId = 56;
//        boolean retrieveByArrivalTime = false;
//        Optional<Message> actualMessage = protocol.receiveMessage(queueId, retrieveByArrivalTime);
//        assertFalse(actualMessage.isPresent());
//    }
//
//    @Test(groups = SMALL, expectedExceptions = MessageProtocolException.class, expectedExceptionsMessageRegExp = ".*than 2 messages.*")
//    public void testReceiveMessageThrowsExceptionIfMoreThanOneMessageIsReceived() throws SQLException {
//        int requestingUserId = 34;
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(requestingUserId, mockedConnection);
//
//        ResultSet mockedResultSet = mock(ResultSet.class);
//        when(mockedResultSet.next()).thenReturn(true).thenReturn(true);
//        when(mockedResultSet.getInt(1)).thenReturn(5);
//        when(mockedResultSet.getInt(2)).thenReturn(3434);
//        when(mockedResultSet.getInt(3)).thenReturn(requestingUserId);
//        when(mockedResultSet.getInt(4)).thenReturn(324);
//        when(mockedResultSet.getTimestamp(5)).thenReturn(Timestamp.valueOf("1999-01-08 04:05:06"));
//        when(mockedResultSet.getString(6)).thenReturn(Utilities.createStringWith(2000, 'c'));
//        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);
//
//        int queueId = 56;
//        boolean retrieveByArrivalTime = false;
//        protocol.receiveMessage(queueId, retrieveByArrivalTime);
//    }
//
//    @Test(groups = SMALL, expectedExceptions = MessageProtocolException.class, expectedExceptionsMessageRegExp = ".*receive message.*")
//    public void testReceiveMessageThrowsException() throws SQLException {
//        int requestingUserId = 34;
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(requestingUserId, mockedConnection);
//
//        int queueId = 56;
//        boolean retrieveByArrivalTime = true;
//
//        ResultSet mockedResultSet = mock(ResultSet.class);
//        when(mockedResultSet.next()).thenReturn(true);
//        when(mockedResultSet.getInt(anyInt())).thenThrow(SQLException.class);
//        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);
//
//        protocol.receiveMessage(queueId, retrieveByArrivalTime);
//    }
//
//    // FIXME ..  more tests for Receive message with sender
//    @Test(groups = SMALL)
//    public void testReceiveMessageWithSender() throws SQLException {
//        int requestingUserId = 34;
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(requestingUserId, mockedConnection);
//
//        int senderId = 99;
//        int queueId = 56;
//        boolean retrieveByArrivalTime = false;
//        String content = Utilities.createStringWith(200, 'A');
//        Message expectedMessage = new Message(senderId, requestingUserId, queueId, Timestamp.valueOf("1999-01-08 04:05:06"), content);
//
//        ResultSet mockedResultSet = mock(ResultSet.class);
//        when(mockedResultSet.next()).thenReturn(true).thenReturn(false); // only returns one row
//        when(mockedResultSet.getInt(1)).thenReturn(5);
//        when(mockedResultSet.getInt(2)).thenReturn(senderId);
//        when(mockedResultSet.getInt(3)).thenReturn(requestingUserId);
//        when(mockedResultSet.getInt(4)).thenReturn(queueId);
//        when(mockedResultSet.getTimestamp(5)).thenReturn(Timestamp.valueOf("1999-01-08 04:05:06"));
//        when(mockedResultSet.getString(6)).thenReturn(content);
//        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);
//
//        Optional<Message> actualMessage = protocol.receiveMessage(senderId, queueId, retrieveByArrivalTime);
//        assertTrue(actualMessage.isPresent());
//        assertEquals(actualMessage.get(), expectedMessage);
//    }
//
//    // FIXME more tests for read message
//    @Test(groups = SMALL)
//    public void testReadMessage() throws SQLException {
//        int requestingUserId = 34;
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(requestingUserId, mockedConnection);
//
//        int queueId = 56;
//        boolean retrieveByArrivalTime = false;
//        String content = Utilities.createStringWith(200, 'A');
//        Message expectedMessage = new Message(234234, requestingUserId, queueId, Timestamp.valueOf("1999-01-08 04:05:06"), content);
//
//        ResultSet mockedResultSet = mock(ResultSet.class);
//        when(mockedResultSet.next()).thenReturn(true).thenReturn(false); // only returns one row
//        when(mockedResultSet.getInt(1)).thenReturn(5);
//        when(mockedResultSet.getInt(2)).thenReturn(234234);
//        when(mockedResultSet.getInt(3)).thenReturn(requestingUserId);
//        when(mockedResultSet.getInt(4)).thenReturn(queueId);
//        when(mockedResultSet.getTimestamp(5)).thenReturn(Timestamp.valueOf("1999-01-08 04:05:06"));
//        when(mockedResultSet.getString(6)).thenReturn(content);
//        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);
//
//        Optional<Message> actualMessage = protocol.readMessage(queueId, retrieveByArrivalTime);
//        assertTrue(actualMessage.isPresent());
//        assertEquals(actualMessage.get(), expectedMessage);
//    }
//
//    // FIXME more test for list queues
//    @Test(groups = SMALL)
//    public void listQueues() throws SQLException {
//        int requestingUserId = 34;
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(requestingUserId, mockedConnection);
//
//        ResultSet mockedResultSet = mock(ResultSet.class);
//        when(mockedResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false); // 3 queues
//        when(mockedResultSet.getInt(1)).thenReturn(5).thenReturn(9).thenReturn(10);
//        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);
//
//        int[] actualQueues = protocol.listQueues();
//        int[] expectedQueues = new int[] {5, 9, 10};
//
//        assertEquals(actualQueues, expectedQueues); // TODO FIXME it seems all assertEquals are swapped, you
//                                                    // first need to pass the values you know for sure
//                                                    // and as second parameter the ones computed
//    }
//
//    @Test(groups = SMALL)
//    public void listQueuesWithEmptyList() throws SQLException {
//        int requestingUserId = 34;
//        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(requestingUserId, mockedConnection);
//
//        ResultSet mockedResultSet = mock(ResultSet.class);
//        when(mockedResultSet.next()).thenReturn(false);
//        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);
//
//        int[] actualQueues = protocol.listQueues();
//        int[] expectedQueues = new int[0];
//
//        assertEquals(actualQueues, expectedQueues);
//    }
}
