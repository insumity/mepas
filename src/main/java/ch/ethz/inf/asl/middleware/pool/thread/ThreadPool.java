package ch.ethz.inf.asl.middleware.pool.thread;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static ch.ethz.inf.asl.utils.Verifier.verifyTrue;

public class ThreadPool implements Executor {

    Executor executor;

    public ThreadPool(int numberOfThreads) {
        verifyTrue(numberOfThreads > 0, "numberOfThreads cannot be negative");

        // make the thread pool threads, daemon threads
        executor = Executors.newFixedThreadPool(numberOfThreads);
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }
}
