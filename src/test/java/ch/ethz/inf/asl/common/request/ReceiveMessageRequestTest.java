package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.ReceiveMessageResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;
import ch.ethz.inf.asl.utils.Optional;
import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class ReceiveMessageRequestTest {

    @Test(groups = SMALL)
    public void testExecuteCallsCorrectProtocolMethod() {
        int requestorId = 324;
        int queueId = 934;
        boolean retrieveByArrivalTime = false;
        Request request = new ReceiveMessageRequest(requestorId, queueId, retrieveByArrivalTime);
        MessagingProtocol protocol = mock(MessagingProtocol.class);
        when(protocol.receiveMessage(queueId, retrieveByArrivalTime)).thenReturn(Optional.<Message>empty());

        assertEquals(request.getRequestorId(), requestorId);

        request.execute(protocol);
        verify(protocol).receiveMessage(queueId, retrieveByArrivalTime);
    }

    @Test(groups = SMALL)
    public void testExecuteCallsCorrectProtocolMethodWithSpecificSender() {
        int requestorId = 324;
        int queueId = 934;
        int senderId = 3911;
        boolean retrieveByArrivalTime = false;
        Request request = new ReceiveMessageRequest(requestorId, senderId, queueId, retrieveByArrivalTime);

        MessagingProtocol protocol = mock(MessagingProtocol.class);
        when(protocol.receiveMessage(senderId, queueId, retrieveByArrivalTime)).thenReturn(Optional.<Message>empty());

        assertEquals(request.getRequestorId(), requestorId);

        request.execute(protocol);
        verify(protocol).receiveMessage(senderId, queueId, retrieveByArrivalTime);
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsFailedResponseIsReturnedWhenExceptionIsThrown() {
        int requestorId = 324;
        int queueId = 934;
        boolean retrieveByArrivalTime = false;
        Request<ReceiveMessageResponse> request = new ReceiveMessageRequest(requestorId, queueId, retrieveByArrivalTime);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        String thrownMessage = "Something is wrong here!";
        when(protocol.receiveMessage(queueId, retrieveByArrivalTime)).thenThrow(new MessagingProtocolException(thrownMessage));

        Response response = request.execute(protocol);
        assertFalse(response.isSuccessful());
        assertEquals(response.getFailedMessage(), thrownMessage);
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsFailedResponseIsReturnedWhenExceptionIsThrownWithSpecificSender() {
        int requestorId = 324;
        int queueId = 934;
        int senderId = 9211;
        boolean retrieveByArrivalTime = false;
        Request<ReceiveMessageResponse> request = new ReceiveMessageRequest(requestorId, senderId, queueId, retrieveByArrivalTime);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        String thrownMessage = "Something is wrong here!";
        when(protocol.receiveMessage(senderId, queueId, retrieveByArrivalTime)).thenThrow(new MessagingProtocolException(thrownMessage));

        Response response = request.execute(protocol);
        assertFalse(response.isSuccessful());
        assertEquals(response.getFailedMessage(), thrownMessage);
    }


    @Test(groups = SMALL)
    public void testExecuteReturnsResponseOfCorrectType() {
        int requestorId = 324;
        int queueId = 934;
        boolean retrieveByArrivalTime = true;
        Request request = new ReceiveMessageRequest(requestorId, queueId, retrieveByArrivalTime);
        MessagingProtocol protocol = mock(MessagingProtocol.class);
        when(protocol.receiveMessage(queueId, retrieveByArrivalTime)).thenReturn(Optional.<Message>empty());

        Response response = request.execute(protocol);
        assertEquals(response.getClass(), ReceiveMessageResponse.class);
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsResponseOfCorrectTypeWithSpecificSender() {
        int requestorId = 324;
        int queueId = 934;
        int senderId = 9132130;
        boolean retrieveByArrivalTime = true;
        Request request = new ReceiveMessageRequest(requestorId, senderId, queueId, retrieveByArrivalTime);
        MessagingProtocol protocol = mock(MessagingProtocol.class);
        when(protocol.receiveMessage(senderId, queueId, retrieveByArrivalTime)).thenReturn(Optional.<Message>empty());

        Response response = request.execute(protocol);
        assertEquals(response.getClass(), ReceiveMessageResponse.class);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testExecuteThrowsExceptionWithNullProtocol() {
        Request request = new ReceiveMessageRequest(1, 2, true);
        request.execute(null);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testExecuteThrowsExceptionWithNullProtocolWithSpecificSender() {
        Request request = new ReceiveMessageRequest(34, 2, 9, true);
        request.execute(null);
    }
}
