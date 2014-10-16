package ch.ethz.inf.asl.utils;

import org.testng.annotations.Test;

import java.util.NoSuchElementException;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.*;

public class OptionalTest {

    @Test(groups = SMALL)
    public void testOf() {
        Optional<Integer> optional = Optional.of(34324);
        assertEquals(optional.get(), new Integer(34324));
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testOfWithNullValue() {
        Optional.of(null);
    }

    @Test(groups = SMALL, expectedExceptions = NoSuchElementException.class)
    public void testEmpty() {
        Optional<Integer> optional = Optional.empty();
        optional.get();
    }

    @Test(groups = SMALL)
    public void testIsPresentWhenThereIsSomething() {
        Optional<String> optional = Optional.of("This is madness!");
        assertTrue(optional.isPresent());
    }

    @Test(groups = SMALL)
    public void testIsPresentWhenItIsEmpty() {
        Optional<String> optional = Optional.empty();
        assertFalse(optional.isPresent());
    }

    @Test(groups = SMALL)
    public void testEquals() {
        Optional<Integer> a = Optional.of(234);
        Optional<Integer> b = Optional.of(234);
        assertEquals(a, b);

        a = Optional.of(243);
        b = Optional.of(23434);
        assertNotEquals(a, b);

        Optional<Integer> empty1 = Optional.empty();
        Optional<Integer> empty2 = Optional.empty();
        assertEquals(empty1, empty2);
    }

    @Test(groups = SMALL)
    public void testEqualsWithDifferentType() {
        assertFalse(Optional.of(324).equals(new Object()));
    }
}
