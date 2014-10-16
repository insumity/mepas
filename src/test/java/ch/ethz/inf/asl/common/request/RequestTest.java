package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.Response;
import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertEquals;

public class RequestTest {

    private class DummyRequest<T extends Response> extends Request {

        public DummyRequest(int requestorId) {
            super(requestorId);
        }

        @Override
        public T execute(MessagingProtocol protocol) {
            return null;
        }
    }

    @Test(groups = SMALL)
    public void testRequest() {
        int requestorId = 234;
        Request request = new DummyRequest(requestorId);
        assertEquals(request.getRequestorId(), requestorId);

        requestorId = -1;
        request = new DummyRequest(requestorId);
        assertEquals(request.getRequestorId(), requestorId);
    }

}
