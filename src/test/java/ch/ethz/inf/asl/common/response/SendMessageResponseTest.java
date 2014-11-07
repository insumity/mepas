package ch.ethz.inf.asl.common.response;

import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertEquals;

public class SendMessageResponseTest {

    @Test(groups = SMALL)
    public void testFailedDeleteQueueResponse() {
        String message = "failed to create";
        Response response = Response.createFailedResponse(message, SendMessageResponse.class);
        assertEquals(response.getFailedMessage(), message);
        assertEquals(response.getClass(), SendMessageResponse.class);
    }
}
