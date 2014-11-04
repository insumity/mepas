package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.ListQueuesResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;
import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class ListQueuesRequestTest {

    @Test(groups = SMALL)
    public void testExecuteCallsCorrectProtocolMethod() {
        int requestorId = -23402324;
        Request request = new ListQueuesRequest(requestorId);
        MessagingProtocol protocol = mock(MessagingProtocol.class);
        when(protocol.listQueues()).thenReturn(new int[] {1, 2});

        assertEquals(request.getRequestorId(), requestorId);

        request.execute(protocol);
        verify(protocol).listQueues();
    }

    @Test(groups = SMALL)
    public void testCorrectListOfQueuesIsReturned() {
        int requestorId = 324;
        Request request = new ListQueuesRequest(requestorId);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        int[] result = new int[] {1, 2, 934, 1};
        when(protocol.listQueues()).thenReturn(result);

        ListQueuesResponse response = (ListQueuesResponse) request.execute(protocol);
        assertEquals(response.getQueues(), result);
    }

    @Test(groups = SMALL)
    public void testEmptyListOfQueuesIsReturned() {
        int requestorId = 324;
        Request request = new ListQueuesRequest(requestorId);
        MessagingProtocol protocol = mock(MessagingProtocol.class);
        when(protocol.listQueues()).thenReturn(new int[] {});

        ListQueuesResponse response = (ListQueuesResponse) request.execute(protocol);
        assertEquals(response.getQueues(), new int[]{});
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsFailedResponseIsReturnedWhenExceptionIsThrown() {
        int requestorId = 324;
        Request<ListQueuesResponse> request = new ListQueuesRequest(requestorId);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        String thrownMessage = "Something is wrong here!";
        when(protocol.listQueues()).thenThrow(new MessagingProtocolException(thrownMessage));

        Response response = request.execute(protocol);
        assertFalse(response.isSuccessful());
        assertEquals(response.getFailedMessage(), thrownMessage);
    }



    @Test(groups = SMALL)
    public void testExecuteReturnsResponseOfCorrectType() {
        int requestorId = 324;
        Request request = new ListQueuesRequest(requestorId);
        MessagingProtocol protocol = mock(MessagingProtocol.class);
        when(protocol.listQueues()).thenReturn(new int[] {1, 2});

        Response response = request.execute(protocol);
        assertEquals(response.getClass(), ListQueuesResponse.class);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testExecuteThrowsExceptionWithNullProtocol() {
        Request request = new ListQueuesRequest(903234234);
        request.execute(null);
    }
}
