package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.DeleteQueueResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;
import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class DeleteQueueRequestTest {

    @Test(groups = SMALL)
    public void testExecuteCallsCorrectProtocolMethod() {
        int requestorId = 324;
        Request request = new DeleteQueueRequest(requestorId, 1);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        assertEquals(request.getRequestorId(), requestorId);

        request.execute(protocol);
        verify(protocol).deleteQueue(1);
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsFailedResponseIsReturnedWhenExceptionIsThrown() {
        int requestorId = 324;
        Request<DeleteQueueResponse> request = new DeleteQueueRequest(requestorId, 2);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        String thrownMessage = "Something is wrong here!";
        doThrow(new MessagingProtocolException(thrownMessage)).when(protocol).deleteQueue(2);

        Response response = request.execute(protocol);
        assertFalse(response.isSuccessful());
        assertEquals(response.getFailedMessage(), thrownMessage);
    }



    @Test(groups = SMALL)
    public void testExecuteReturnsResponseOfCorrectType() {
        int requestorId = 324;
        Request request = new DeleteQueueRequest(requestorId, 9);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        Response response = request.execute(protocol);
        assertEquals(response.getClass(), DeleteQueueResponse.class);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testExecuteThrowsExceptionWithNullProtocol() {
        Request request = new DeleteQueueRequest(34, 2);
        request.execute(null);
    }
}
