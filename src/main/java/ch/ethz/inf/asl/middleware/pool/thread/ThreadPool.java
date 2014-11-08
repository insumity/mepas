package ch.ethz.inf.asl.middleware.pool.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import static ch.ethz.inf.asl.utils.Verifier.verifyTrue;

public class ThreadPool implements Executor {

    private Thread[] workingThreads;
    private BlockingQueue<Runnable> runnableTasks;

    private class WorkingRunnable implements Runnable {
        @Override
        public void run() {

            while (true) {
                Runnable runnable = null;
                try {
                    runnable = runnableTasks.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runnable.run();
            }
        }
    }

    public ThreadPool(int numberOfThreads) {
        verifyTrue(numberOfThreads > 0, "Given numberOfThreads cannot be negative!");

        runnableTasks = new ArrayBlockingQueue<>(numberOfThreads);
        workingThreads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; ++i) {
            workingThreads[i] = new Thread(new WorkingRunnable());
            workingThreads[i].start();
        }
    }

    @Override
    public void execute(Runnable command) {

        try {
            runnableTasks.put(command);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
