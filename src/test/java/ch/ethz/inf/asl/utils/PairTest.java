package ch.ethz.inf.asl.utils;


import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertEquals;

public class PairTest {

    @Test(groups = SMALL)
    public void testPairGetters() {
        Integer first = new Integer(324430012);
        Object second = new Object();

        Pair pair = new Pair(first, second);
        assertEquals(pair.getFirst(), first);
        assertEquals(pair.getSecond(), second);
    }

    @Test(groups = SMALL)
    public void testPairSetters() {
        Pair pair = new Pair();

        String first = "This is a string!";
        String second = "This is the second part";

        pair.setFirst(first);
        pair.setSecond(second);

        assertEquals(pair.getFirst(), first);
        assertEquals(pair.getSecond(), second);
    }

}
