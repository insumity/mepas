package ch.ethz.inf.asl.utils;

import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class VerifierTest {

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testNotNullWithNullObject() {
        Verifier.notNull(null, "");
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = ".*message cannot be null.*")
    public void testNotNullWithNullMessage() {
        Verifier.notNull(new Object(), null);
    }

    @Test(groups = SMALL)
    public void testNotNullWithNotNullObjectThrowsNoException() {
        Verifier.notNull(new Object(), "");
    }

    @Test(groups = SMALL)
    public void testNotNull() {
        String thrownMessage = "this is a message";
        try {
            Verifier.notNull(null, thrownMessage);
            fail();
        }
        catch (NullPointerException npe) {
            assertEquals(npe.getMessage(), thrownMessage);
        }
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testHasTextWithNullObject() {
        Verifier.hasText(null, "has text");
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testHasTextWithNullMessage() {
        Verifier.hasText("has actually text", null);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class)
    public void testHasTextWithEmptyString() {
        Verifier.hasText("", "message to be thrown");
    }

    @Test(groups = SMALL)
    public void testHasText1() {
        String thrownMessage = "this is a message";
        try {
            Verifier.hasText("", thrownMessage);
            fail();
        }
        catch (IllegalArgumentException iae) {
            assertEquals(iae.getMessage(), thrownMessage);
        }
    }

    @Test(groups = SMALL)
    public void testHasText2() {
        Verifier.hasText("text", "message to be thrown");
    }


    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testVerifyTrueWithNullMessage() {
        Verifier.verifyTrue(true, null);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class)
    public void testVerifyTrueWhenFalse() {
        Verifier.verifyTrue(false, "What's going on?");
    }

    @Test(groups = SMALL)
    public void testVerifyTru1() {
        String thrownMessage = "this is a message";
        try {
            Verifier.verifyTrue(false, thrownMessage);
            fail();
        }
        catch (IllegalArgumentException iae) {
            assertEquals(iae.getMessage(), thrownMessage);
        }
    }

    @Test(groups = SMALL)
    public void testVerifyTrue2() {
        Verifier.verifyTrue(true, "message to be thrown");
    }
}