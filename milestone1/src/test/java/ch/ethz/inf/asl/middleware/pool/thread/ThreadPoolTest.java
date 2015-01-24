package ch.ethz.inf.asl.middleware.pool.thread;


import org.testng.annotations.Test;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertTrue;

public class ThreadPoolTest {

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class)
    public void testCreationOfPoolWithNegativeNumber() {
        new ThreadPool(-1);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class)
    public void testCreationOfPoolWithZeroNumber() {
        new ThreadPool(0);
    }

    @Test(groups = SMALL)
    public void testThatCommandIsExecuted() throws InterruptedException {
        ThreadPool pool = new ThreadPool(2);

        final boolean[] commandExecuted = {false};

        Runnable command = new Runnable() {

            @Override
            public void run() {
                commandExecuted[0] = true;
            }
        };

        pool.execute(command);
        Thread.sleep(100);
        assertTrue(commandExecuted[0]);
    }
}
