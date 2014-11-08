package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.common.response.SendMessageResponse;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;
import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class SendMessageRequestTest {

    @Test(groups = SMALL)
    public void testExecuteCallsCorrectProtocolMethod() {
        int requestorId = 324;
        int queueId = 934;
        String content = "This is some content for the masses!";
        Request request = new SendMessageRequest(requestorId, queueId, content);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        assertEquals(request.getRequestorId(), requestorId);

        request.execute(protocol);
        verify(protocol).sendMessage(queueId, content);
    }

    @Test(groups = SMALL)
    public void testExecuteCallsCorrectProtocolMethodWithSpecificReceiver() {
        int requestorId = 324;
        int queueId = 934;
        int receiverId = -9132313;
        String content = "This is some content for the masses!";
        Request request = new SendMessageRequest(requestorId, receiverId, queueId, content);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        assertEquals(request.getRequestorId(), requestorId);

        request.execute(protocol);
        verify(protocol).sendMessage(receiverId, queueId, content);
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsFailedResponseIsReturnedWhenExceptionIsThrown() {
        int requestorId = 324;
        int queueId = 934;
        String content = "This is some content for the masses!";

        Request<SendMessageResponse> request = new SendMessageRequest(requestorId, queueId, content);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        String thrownMessage = "Something is wrong here!";
        doThrow(new MessagingProtocolException(thrownMessage)).when(protocol).sendMessage(queueId, content);

        Response response = request.execute(protocol);
        assertFalse(response.isSuccessful());
        assertEquals(response.getFailedMessage(), thrownMessage);
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsFailedResponseIsReturnedWhenExceptionIsThrownWithSpecificReceiver() {
        int requestorId = 324;
        int receiverId = 132;
        int queueId = 934;
        String content = "This is some content for the masses!";

        Request<SendMessageResponse> request = new SendMessageRequest(requestorId, receiverId, queueId, content);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        String thrownMessage = "Something is wrong here!";
        doThrow(new MessagingProtocolException(thrownMessage)).when(protocol).sendMessage(receiverId, queueId, content);

        Response response = request.execute(protocol);
        assertFalse(response.isSuccessful());
        assertEquals(response.getFailedMessage(), thrownMessage);
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsResponseOfCorrectType() {
        int requestorId = 324;
        int queueId = 934;
        String content = "This is some content for the masses!";

        Request request = new SendMessageRequest(requestorId, queueId, content);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        Response response = request.execute(protocol);
        assertEquals(response.getClass(), SendMessageResponse.class);
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsResponseOfCorrectTypeWithSpecificReceiver() {
        int requestorId = 324;
        int queueId = 934;
        int receiverId = 901321;
        String content = "This is some content for the masses!";

        Request request = new SendMessageRequest(requestorId, receiverId, queueId, content);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        Response response = request.execute(protocol);
        assertEquals(response.getClass(), SendMessageResponse.class);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testExecuteThrowsExceptionWithNullProtocol() {
        Request request = new SendMessageRequest(9, 123, "It is a message indeed!");
        request.execute(null);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testExecuteThrowsExceptionWithNullProtocolWithSpecificReceiver() {
        Request request = new SendMessageRequest(1, 4, 2343, "This is a message!");
        request.execute(null);
    }
}
