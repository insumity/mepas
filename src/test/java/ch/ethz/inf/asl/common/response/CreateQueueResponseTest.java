package ch.ethz.inf.asl.common.response;

import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertEquals;

public class CreateQueueResponseTest {

    @Test(groups = SMALL)
    public void testCreateQueueResponse() {
        int queueId = 234234;
        CreateQueueResponse response = new CreateQueueResponse(queueId);
        assertEquals(response.getQueueId(), queueId);
    }

    @Test(groups = SMALL)
    public void testFailedCreateQueueResponse() {
        String message = "failed to create";
        Response response = Response.createFailedResponse(message, CreateQueueResponse.class);
        assertEquals(response.getFailedMessage(), message);
        assertEquals(response.getClass(), CreateQueueResponse.class);
    }
}
