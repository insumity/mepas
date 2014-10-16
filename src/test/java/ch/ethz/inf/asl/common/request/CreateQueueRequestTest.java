package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.CreateQueueResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;
import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class CreateQueueRequestTest {

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testCreateQueueRequestWithNullName() {
        new CreateQueueRequest(34, null);
    }

    @Test(groups = SMALL)
    public void testExecuteCallsCorrectProtocolMethod() {
        String queueName = "some name";
        Request request = new CreateQueueRequest(34, queueName);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        assertEquals(request.getRequestorId(), 34);

        request.execute(protocol);
        verify(protocol).createQueue(queueName);
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsFailedResponseIsReturnedWhenExceptionIsThrown() {
        String queueName = "some name";
        Request<CreateQueueResponse> request = new CreateQueueRequest(34, queueName);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        String thrownMessage = "Something is wrong here!";
        when(protocol.createQueue(queueName)).thenThrow(new MessageProtocolException(thrownMessage));

        Response response = request.execute(protocol);
        assertFalse(response.isSuccessful());
        assertEquals(response.getFailedMessage(), thrownMessage);
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsResponseOfCorrectType() {
        Request request = new CreateQueueRequest(34, "some name");
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        Response response = request.execute(protocol);
        assertEquals(response.getClass(), CreateQueueResponse.class);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testExecuteThrowsExceptionWithNullProtocol() {
        Request request = new CreateQueueRequest(34, "some name");
        request.execute(null);
    }
}
