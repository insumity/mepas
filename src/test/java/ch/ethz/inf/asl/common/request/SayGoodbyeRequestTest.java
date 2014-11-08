package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.common.response.SayGoodbyeResponse;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;
import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class SayGoodbyeRequestTest {

    @Test(groups = SMALL)
    public void testExecuteCallsCorrectProtocolMethod() {
        int requestorId = 324;
        Request request = new SayGoodbyeRequest(requestorId);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        assertEquals(request.getRequestorId(), requestorId);
        request.execute(protocol);
        verify(protocol).sayGoodbye();
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsFailedResponseIsReturnedWhenExceptionIsThrown() {
        int requestorId = 324;
        Request<SayGoodbyeResponse> request = new SayGoodbyeRequest(requestorId);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        String thrownMessage = "Something is wrong here!";
        doThrow(new MessagingProtocolException(thrownMessage)).when(protocol).sayGoodbye();

        Response response = request.execute(protocol);
        assertFalse(response.isSuccessful());
        assertEquals(response.getFailedMessage(), thrownMessage);
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsResponseOfCorrectType() {
        Request request = new SayGoodbyeRequest(98113);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        Response response = request.execute(protocol);
        assertEquals(response.getClass(), SayGoodbyeResponse.class);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testExecuteThrowsExceptionWithNullProtocol() {
        Request request = new SayGoodbyeRequest(34);
        request.execute(null);
    }
}
