package ch.ethz.inf.asl.common.response;

import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertEquals;

public class SayHelloResponseTest {

    @Test(groups = SMALL)
    public void testSayHelloResponse() {
        int clientId = 30934;
        SayHelloResponse response = new SayHelloResponse(clientId);
        assertEquals(response.getClientId(), clientId);
    }

    @Test(groups = SMALL)
    public void testFailedSayHelloResponse() {
        String message = "failed to create";
        Response response = Response.createFailedResponse(message, SayHelloResponse.class);
        assertEquals(response.getFailedMessage(), message);
        assertEquals(response.getClass(), SayHelloResponse.class);
    }
}
