package ch.ethz.inf.asl.common.response;

import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ResponseTest {

    private class DummyResponse extends Response {

    }

    @Test(groups = SMALL)
    public void testCreationOfResponse() {
        Response response = new DummyResponse();
        assertTrue(response.isSuccessful());
    }

    @Test(groups = SMALL)
    public void testCreationOfFailedResponse() {
        String failedMessage = "The request failed!";
        Response response = Response.createFailedResponse(failedMessage, CreateQueueResponse.class);
        assertFalse(response.isSuccessful());
        assertEquals(response.getFailedMessage(), failedMessage);
        assertEquals(response.getClass(), CreateQueueResponse.class);
    }
}
