package ch.ethz.inf.asl.common.response;

import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertEquals;

public class ReceiveMessageResponseTest {

    @Test(groups = SMALL)
    public void testReceiveMessageResponse() {
        String message = "failed to create";
        Response response = Response.createFailedResponse(message, ReceiveMessageResponse.class);
        assertEquals(response.getFailedMessage(), message);
        assertEquals(response.getClass(), ReceiveMessageResponse.class);
    }
}
