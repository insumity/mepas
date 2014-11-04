package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.common.response.SayGoodbyeResponse;
import ch.ethz.inf.asl.common.response.SayHelloResponse;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;
import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class SayHelloRequestTest {

    @Test(groups = SMALL)
    public void testExecuteCallsCorrectProtocolMethod() {
        String clientName = "some name!";
        Request request = new SayHelloRequest(clientName);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        request.execute(protocol);
        verify(protocol).sayHello(clientName);
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsFailedResponseIsReturnedWhenExceptionIsThrown() {
        String clientName = "someName";
        Request<SayHelloResponse> request = new SayHelloRequest(clientName);
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        String thrownMessage = "Something is wrong here!";
        doThrow(new MessagingProtocolException(thrownMessage)).when(protocol).sayHello(clientName);

        Response response = request.execute(protocol);
        assertFalse(response.isSuccessful());
        assertEquals(response.getFailedMessage(), thrownMessage);
    }

    @Test(groups = SMALL)
    public void testExecuteReturnsResponseOfCorrectType() {
        Request request = new SayHelloRequest("foo bar");
        MessagingProtocol protocol = mock(MessagingProtocol.class);

        Response response = request.execute(protocol);
        assertEquals(response.getClass(), SayHelloResponse.class);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testExecuteThrowsExceptionWithNullProtocol() {
        Request request = new SayHelloRequest("foobarbaz");
        request.execute(null);
    }
}
