package ch.ethz.inf.asl.common;

import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.NoSuchElementException;

import static ch.ethz.inf.asl.common.MessageConstants.MAXIMUM_MESSAGE_LENGTH;
import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.*;

public class MessageTest {

    private static final String MESSAGE_CONTENT = "this is the content of a message! Let's include some more characters" +
            "just for the fun of it: Αυτοί είναι ελληνικοί χαρακτήρες!";

    // should be NullPointerException ?? TODO FIXME or Ille
    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testCannotCreateMessageWithNullTimestamp() {
        new Message(11, 34, 2, null, "");
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testCannotCreateMessageWithNullContent() {
        new Message(11, 34, 2, Timestamp.valueOf("2012-03-03 11:22:12"), null);
    }

    @Test(groups = SMALL)
    public void testGetSenderId() {
        Message msg =  new Message(11, 34, 2, Timestamp.valueOf("2012-03-03 11:22:12"), MESSAGE_CONTENT);
        assertEquals(msg.getSenderId(), 11);
    }

    @Test(groups = SMALL)
    public void testGetReceiverId() {
        Message msg =  new Message(11, 34, 2, Timestamp.valueOf("2012-03-03 11:22:12"), MESSAGE_CONTENT);
        assertEquals(msg.getReceiverId(), 34);
    }
    @Test(groups = SMALL, expectedExceptions = NoSuchElementException.class)
    public void testGetReceiverIdWhenNull() {
        Message msg =  new Message(11, 2, Timestamp.valueOf("2012-03-03 11:22:12"), MESSAGE_CONTENT);
        msg.getReceiverId();
    }

    @Test(groups = SMALL)
    public void testHasReceiverWhenThereIsOne() {
        Message msg =  new Message(211, 34, 2, Timestamp.valueOf("2012-03-03 11:22:12"), MESSAGE_CONTENT);
        assertTrue(msg.hasReceiver());
    }

    @Test(groups = SMALL)
    public void testHasReceiverWhenThereIsNotOne() {
        Message msg =  new Message(11, 2, Timestamp.valueOf("2012-03-03 11:22:12"), MESSAGE_CONTENT);
        assertFalse(msg.hasReceiver());
    }

    @Test(groups = SMALL)
    public void testGetQueueId() {
        Message msg =  new Message(11, 2, Timestamp.valueOf("2012-03-03 11:22:12"), MESSAGE_CONTENT);
        assertEquals(msg.getQueueId(), 2);
    }

    @Test(groups = SMALL)
    public void testGetArrivalTime() {
        Message msg =  new Message(11, 2, Timestamp.valueOf("2012-03-03 11:22:12"), MESSAGE_CONTENT);
        assertEquals(msg.getArrivalTime(), Timestamp.valueOf("2012-03-03 11:22:12"));
    }

    @Test(groups = SMALL)
    public void testGetContent() {
        Message msg =  new Message(11,  2, Timestamp.valueOf("2012-03-03 11:22:12"), MESSAGE_CONTENT);
        assertEquals(msg.getContent(), MESSAGE_CONTENT);
    }

    @Test(groups = SMALL)
    public void testEquals() {
        Message msg1 = new Message(11, 2, Timestamp.valueOf("2012-03-03 11:22:12"), MESSAGE_CONTENT);
        Message msg2 = new Message(11, 2, Timestamp.valueOf("2012-03-03 11:22:12"), MESSAGE_CONTENT);

        assertEquals(msg1, msg2);
        assertEquals(msg2, msg1);

        msg1 = new Message(11, 2314234, 2, Timestamp.valueOf("2012-03-03 11:22:12"), MESSAGE_CONTENT);
        msg2 = new Message(11, 2314234, 2, Timestamp.valueOf("2012-03-03 11:22:12"), MESSAGE_CONTENT);

        assertEquals(msg1, msg2);
        assertEquals(msg2, msg1);

        assertEquals(msg1, msg1);
        assertEquals(msg2, msg2);
    }

    @Test(groups = SMALL)
         public void testEqualsWithDifferentMesages() {
        Message msg1 =  new Message(11, 3, 2, Timestamp.valueOf("2012-03-03 21:22:12"), MESSAGE_CONTENT);
        Message msg2 =  new Message(12, 3, 2, Timestamp.valueOf("2012-03-03 21:22:12"), MESSAGE_CONTENT);
        assertNotEquals(msg1, msg2);

        msg1 =  new Message(11, 3, 2, Timestamp.valueOf("2012-03-03 21:22:12"), MESSAGE_CONTENT);
        msg2 =  new Message(11, 2, Timestamp.valueOf("2012-03-03 21:22:12"), MESSAGE_CONTENT);
        assertNotEquals(msg1, msg2);

        msg1 =  new Message(11, 2, Timestamp.valueOf("2012-03-03 21:22:12"), MESSAGE_CONTENT);
        msg2 =  new Message(11, 5, 2, Timestamp.valueOf("2012-03-03 21:22:12"), MESSAGE_CONTENT);
        assertNotEquals(msg1, msg2);

        msg1 =  new Message(-211, 10132, 2, Timestamp.valueOf("2012-03-03 21:22:12"), MESSAGE_CONTENT);
        msg2 =  new Message(-211, 10132, 5, Timestamp.valueOf("2012-03-03 21:22:12"), MESSAGE_CONTENT);
        assertNotEquals(msg1, msg2);

        msg1 =  new Message(-211, 10132, 5, Timestamp.valueOf("2012-03-03 22:22:12"), MESSAGE_CONTENT);
        msg2 =  new Message(-211, 10132, 5, Timestamp.valueOf("2012-03-03 22:22:11"), MESSAGE_CONTENT);
        assertNotEquals(msg1, msg2);

        msg1 =  new Message(-211, 10132, 5, Timestamp.valueOf("2012-03-03 22:22:12"), MESSAGE_CONTENT);
        msg2 =  new Message(-211, 10132, 5, Timestamp.valueOf("2012-03-03 22:22:11"), MESSAGE_CONTENT.replace('a', 'b'));
        assertNotEquals(msg1, msg2);
    }

    @Test(groups = SMALL)
    public void testEqualsWithDifferentTypes() {
        Message msg =  new Message(11, 3, 2, Timestamp.valueOf("2012-03-03 21:22:12"), MESSAGE_CONTENT);
        assertFalse(msg.equals(new Object()));
    }

    @Test(groups = SMALL)
    public void testHashCode() {
        Message msg1 = new Message(2341, 24324110, 5695461, Timestamp.valueOf("2014-09-08 23:12:59"), MESSAGE_CONTENT);
        Message msg2 = new Message(2341, 24324110, 5695461, Timestamp.valueOf("2014-09-08 23:12:59"), MESSAGE_CONTENT);

        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());

        msg1 = new Message(112391, 9912, Timestamp.valueOf("2014-10-03 11:02:12"), MESSAGE_CONTENT);
        msg2 = new Message(112391, 9912, Timestamp.valueOf("2014-10-03 11:02:12"), MESSAGE_CONTENT);

        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());
    }

}
