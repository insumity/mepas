package ch.ethz.inf.asl;

import ch.ethz.inf.asl.utils.Optional;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.NoSuchElementException;

import static ch.ethz.inf.asl.utils.TestConstants.SMALL;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertEquals;

public class MessageTest {

    private String STRING_OF_200_CHARACTERS = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testCannotCreateMessageWithNullReceiverId() {
        new Message(11, null, 2, Timestamp.valueOf("2012-03-03 11:22:12"), null);
    }

    // should be NullPointerException ?? TODO FIXME or Ille
    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testCannotCreateMessageWithNullTimestamp() {
        new Message(11, Optional.of(34), 2, null, "");
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testCannotCreateMessageWithNullContent() {
        new Message(11, Optional.of(34), 2, Timestamp.valueOf("2012-03-03 11:22:12"), null);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class)
    public void testCannotCreateMessageWithEmptyContent() {
        new Message(11, Optional.of(34), 2, Timestamp.valueOf("2012-03-03 11:22:12"), "");
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class)
    public void testCannotCreateMessageWithContentOfInvalidLength() {
        new Message(11, Optional.of(34), 2, Timestamp.valueOf("2012-03-03 11:22:12"), "some content");
    }

    @Test(groups = SMALL)
    public void testGetSenderId() {
        Message msg =  new Message(11, Optional.of(34), 2, Timestamp.valueOf("2012-03-03 11:22:12"), STRING_OF_200_CHARACTERS);
        assertEquals(msg.getSenderId(), 11);
    }

    @Test(groups = SMALL)
    public void testGetReceiverId() {
        Message msg =  new Message(11, Optional.of(34), 2, Timestamp.valueOf("2012-03-03 11:22:12"), STRING_OF_200_CHARACTERS);
        assertEquals(msg.getReceiverId(), new Integer(34));
    }
    @Test(groups = SMALL, expectedExceptions = NoSuchElementException.class)
    public void testGetReceiverIdWhenNull() {
        Message msg =  new Message(11, Optional.<Integer>empty(), 2, Timestamp.valueOf("2012-03-03 11:22:12"), STRING_OF_200_CHARACTERS);
        msg.getReceiverId();
    }

    @Test(groups = SMALL)
    public void testHasReceiverWhenThereIsOne() {
        Message msg =  new Message(211, Optional.of(34), 2, Timestamp.valueOf("2012-03-03 11:22:12"), STRING_OF_200_CHARACTERS);
        assertTrue(msg.hasReceiver());
    }

    @Test(groups = SMALL)
    public void testHasReceiverWhenThereIsNotOne() {
        Message msg =  new Message(11, Optional.<Integer>empty(), 2, Timestamp.valueOf("2012-03-03 11:22:12"), STRING_OF_200_CHARACTERS);
        assertFalse(msg.hasReceiver());
    }

    @Test(groups = SMALL)
    public void testGetQueueId() {
        Message msg =  new Message(11, Optional.<Integer>empty(), 2, Timestamp.valueOf("2012-03-03 11:22:12"), STRING_OF_200_CHARACTERS);
        assertEquals(msg.getQueueId(), 2);
    }

    @Test(groups = SMALL)
    public void testGetArrivalTime() {
        Message msg =  new Message(11, Optional.<Integer>empty(), 2, Timestamp.valueOf("2012-03-03 11:22:12"), STRING_OF_200_CHARACTERS);
        assertEquals(msg.getArrivalTime(), Timestamp.valueOf("2012-03-03 11:22:12"));
    }

    @Test(groups = SMALL)
    public void testGetContent() {
        Message msg =  new Message(11, Optional.<Integer>empty(), 2, Timestamp.valueOf("2012-03-03 11:22:12"), STRING_OF_200_CHARACTERS);
        assertEquals(msg.getContent(), STRING_OF_200_CHARACTERS);
    }

    @Test(groups = SMALL)
    public void testEquals() {
        Message msg1 = new Message(11, Optional.<Integer>empty(), 2, Timestamp.valueOf("2012-03-03 11:22:12"), STRING_OF_200_CHARACTERS);
        Message msg2 = new Message(11, Optional.<Integer>empty(), 2, Timestamp.valueOf("2012-03-03 11:22:12"), STRING_OF_200_CHARACTERS);

        assertEquals(msg1, msg2);
        assertEquals(msg2, msg1);

        msg1 = new Message(11, Optional.of(2314234), 2, Timestamp.valueOf("2012-03-03 11:22:12"), STRING_OF_200_CHARACTERS);
        msg2 = new Message(11, Optional.of(2314234), 2, Timestamp.valueOf("2012-03-03 11:22:12"), STRING_OF_200_CHARACTERS);

        assertEquals(msg1, msg2);
        assertEquals(msg2, msg1);

        assertEquals(msg1, msg1);
        assertEquals(msg2, msg2);
    }

    @Test(groups = SMALL)
    public void testEqualsWithDifferentMesages() {
        Message msg1 =  new Message(11, Optional.of(3), 2, Timestamp.valueOf("2012-03-03 21:22:12"), STRING_OF_200_CHARACTERS);
        Message msg2 =  new Message(12, Optional.of(3), 2, Timestamp.valueOf("2012-03-03 21:22:12"), STRING_OF_200_CHARACTERS);
        assertNotEquals(msg1, msg2);

        msg1 =  new Message(11, Optional.of(3), 2, Timestamp.valueOf("2012-03-03 21:22:12"), STRING_OF_200_CHARACTERS);
        msg2 =  new Message(11, Optional.<Integer>empty(), 2, Timestamp.valueOf("2012-03-03 21:22:12"), STRING_OF_200_CHARACTERS);
        assertNotEquals(msg1, msg2);

        msg1 =  new Message(11, Optional.<Integer>empty(), 2, Timestamp.valueOf("2012-03-03 21:22:12"), STRING_OF_200_CHARACTERS);
        msg2 =  new Message(11, Optional.of(5), 2, Timestamp.valueOf("2012-03-03 21:22:12"), STRING_OF_200_CHARACTERS);
        assertNotEquals(msg1, msg2);

        msg1 =  new Message(-211, Optional.of(10132), 2, Timestamp.valueOf("2012-03-03 21:22:12"), STRING_OF_200_CHARACTERS);
        msg2 =  new Message(-211, Optional.of(10132), 5, Timestamp.valueOf("2012-03-03 21:22:12"), STRING_OF_200_CHARACTERS);
        assertNotEquals(msg1, msg2);

        msg1 =  new Message(-211, Optional.of(10132), 5, Timestamp.valueOf("2012-03-03 22:22:12"), STRING_OF_200_CHARACTERS);
        msg2 =  new Message(-211, Optional.of(10132), 5, Timestamp.valueOf("2012-03-03 22:22:11"), STRING_OF_200_CHARACTERS);
        assertNotEquals(msg1, msg2);

        msg1 =  new Message(-211, Optional.of(10132), 5, Timestamp.valueOf("2012-03-03 22:22:12"), STRING_OF_200_CHARACTERS);
        msg2 =  new Message(-211, Optional.of(10132), 5, Timestamp.valueOf("2012-03-03 22:22:11"), STRING_OF_200_CHARACTERS.replace('a', 'b'));
        assertNotEquals(msg1, msg2);
    }

    @Test(groups = SMALL)
    public void testHashCode() {
        Message msg1 = new Message(2341, Optional.of(24324110), 5695461, Timestamp.valueOf("2014-09-08 23:12:59"), STRING_OF_200_CHARACTERS);
        Message msg2 = new Message(2341, Optional.of(24324110), 5695461, Timestamp.valueOf("2014-09-08 23:12:59"), STRING_OF_200_CHARACTERS);

        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());

        msg1 = new Message(112391, Optional.<Integer>empty(), 9912, Timestamp.valueOf("2014-10-03 11:02:12"), STRING_OF_200_CHARACTERS);
        msg2 = new Message(112391, Optional.<Integer>empty(), 9912, Timestamp.valueOf("2014-10-03 11:02:12"), STRING_OF_200_CHARACTERS);

        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());
    }

}
