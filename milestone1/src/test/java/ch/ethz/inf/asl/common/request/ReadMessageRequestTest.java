package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.ReadMessageResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;
import ch.ethz.inf.asl.utils.Optional;
import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class ReadMessageRequestTest {

    @Test(groups = SMALL)
    public void testExecuteCallsCorrectProtocolMethod() {
        int requestorId = 324;
        int queueId = 934;
        boolean retrieveByArrivalTime = false;
        Request request = new ReadMessageRequest(requestorId, queueId, retrieveByArrivalTime);
        MessagingProtocol protocol = mock(MessagingProtocol.class);
        when(protocol.readMessage(queueId, retrieveByArrivalTime)).thenReturn(Optional.<Message>empty());

        assertEquals(request.getRequestorId(), requestorId);

        request.execute(protocol);
        verify(protocol).readMessage(queueId, retrieveByArrivalTime);
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsFailedResponseIsReturnedWhenExceptionIsThrown() {
        int requestorId = 324;
        int queueId = 934;
        boolean retrieveByArrivalTime = false;
        Request<ReadMessageResponse> request = new ReadMessageRequest(requestorId, queueId, retrieveByArrivalTime);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        String thrownMessage = "Something is wrong here!";
        when(protocol.readMessage(queueId, retrieveByArrivalTime)).thenThrow(new MessagingProtocolException(thrownMessage));

        Response response = request.execute(protocol);
        assertFalse(response.isSuccessful());
        assertEquals(response.getFailedMessage(), thrownMessage);
    }



    @Test(groups = SMALL)
    public void testExecuteReturnsResponseOfCorrectType() {
        int requestorId = 324;
        int queueId = 934;
        boolean retrieveByArrivalTime = true;
        Request request = new ReadMessageRequest(requestorId, queueId, retrieveByArrivalTime);
        MessagingProtocol protocol = mock(MessagingProtocol.class);
        when(protocol.readMessage(queueId, retrieveByArrivalTime)).thenReturn(Optional.<Message>empty());

        Response response = request.execute(protocol);
        assertEquals(response.getClass(), ReadMessageResponse.class);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testExecuteThrowsExceptionWithNullProtocol() {
        Request request = new ReadMessageRequest(34, 2, true);
        request.execute(null);
    }
}
