package ch.ethz.inf.asl.common.response;

import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertEquals;

public class ListQueuesResponseTest {

    @Test(groups = SMALL)
    public void testListQueuesResponse() {
        int[] queues = new int[]{ 9, 8, 4};
        ListQueuesResponse response = new ListQueuesResponse(queues);
        assertEquals(response.getQueues(), queues);
    }


    @Test(groups = SMALL)
    public void testListQueuesResponseWithFailedMessage() {
        String message = "failed to create";
        Response response = Response.createFailedResponse(message, ListQueuesResponse.class);
        assertEquals(response.getFailedMessage(), message);
        assertEquals(response.getClass(), ListQueuesResponse.class);
    }
}
