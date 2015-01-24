package ch.ethz.inf.asl.common.response;

import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertEquals;

public class DeleteQueueResponseTest {

    @Test(groups = SMALL)
    public void testFailedDeleteQueueResponse() {
        String message = "failed to create";
        Response response = Response.createFailedResponse(message, DeleteQueueResponse.class);
        assertEquals(response.getFailedMessage(), message);
        assertEquals(response.getClass(), DeleteQueueResponse.class);
    }
}
