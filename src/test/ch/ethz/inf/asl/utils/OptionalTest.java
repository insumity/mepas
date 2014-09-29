package ch.ethz.inf.asl.utils;

import org.testng.annotations.Test;

import java.util.NoSuchElementException;

import static ch.ethz.inf.asl.utils.TestConstants.SMALL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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
        Optional<String> optional = Optional.of("Sfdsf");
        assertTrue(optional.isPresent());
    }

    @Test(groups = SMALL)
    public void testIsPresentWhenItIsEmpty() {
        Optional<String> optional = Optional.empty();
        assertFalse(optional.isPresent());
    }
}
