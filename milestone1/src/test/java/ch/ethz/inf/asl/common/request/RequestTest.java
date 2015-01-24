package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.Response;
import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertEquals;

public class RequestTest {

    private class DummyResponse extends Response {
    }

    private class DummyRequest extends Request<DummyResponse> {

        public DummyRequest(int requestorId) {
            super(requestorId);
        }

        @Override
        public DummyResponse execute(MessagingProtocol protocol) {
            return null;
        }

        @Override
        public String getName() {
            return "Something";
        }
    }

    @Test(groups = SMALL)
    public void testRequestorIdIsSetCorrectly() {
        DummyRequest request = new DummyRequest(234);
        assertEquals(request.getRequestorId(), 234);
    }

}
