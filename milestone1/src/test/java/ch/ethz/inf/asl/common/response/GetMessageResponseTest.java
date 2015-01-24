package ch.ethz.inf.asl.common.response;

import ch.ethz.inf.asl.common.Message;
import org.testng.annotations.Test;

import java.sql.Timestamp;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class GetMessageResponseTest {

    private class DummyGetMessageResponse extends GetMessageResponse {
        public DummyGetMessageResponse() {
            super();
        }

        public DummyGetMessageResponse(Message msg) {
            super(msg);
        }
    }

    @Test(groups = SMALL)
    public void testGetMessageResponse() {
        GetMessageResponse noMessage = new DummyGetMessageResponse();
        assertNull(noMessage.getMessage());

        Timestamp timestamp = Timestamp.valueOf("2007-09-23 10:10:10.0");
        Message msg = new Message(34, 34, 13, timestamp, "foo");
        GetMessageResponse message = new DummyGetMessageResponse(msg);
        assertEquals(message.getMessage(), msg);
    }
}
