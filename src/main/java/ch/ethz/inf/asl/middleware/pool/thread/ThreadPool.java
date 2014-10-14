package ch.ethz.inf.asl.middleware.pool.thread;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static ch.ethz.inf.asl.utils.Helper.verifyTrue;

public class ThreadPool implements Executor {

    Executor executor;

    public ThreadPool(int numberOfThreads) {
        verifyTrue(numberOfThreads > 0, "numberOfThreads cannot be negative");

        executor = Executors.newFixedThreadPool(numberOfThreads);
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }
}
