package threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadPool implements Executor {

    private static final Runnable SHUTDOWN_TASK = () -> {
    };

    private final BlockingQueue<Runnable> queue = new LinkedTransferQueue<>();
    private final Thread[] threads;
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean shutdown = new AtomicBoolean();

    public ThreadPool(int numThreads) {
        threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (; ; ) {
                    try {
                        final Runnable task = queue.take();
                        if (task == SHUTDOWN_TASK) {
                            break;
                        }

                        task.run();
                    } catch (Throwable t) {
                        if (!(t instanceof InterruptedException)) {
                            System.err.println(
                                    "Exception in thread '" + Thread.currentThread().getName() + "'");
                            t.printStackTrace();
                        }
                    }
                }

                System.err.println("Thread '" + Thread.currentThread().getName() + "' is shutting down");
            });
        }
    }

    @Override
    public void execute(Runnable command) {
        if (started.compareAndSet(false, true)) {
            for (Thread thread : threads) {
                thread.start();
            }
        }

        if (shutdown.get()) {
            throw new RejectedExecutionException();
        }

        queue.add(command);

        if (shutdown.get()) {
            queue.remove(command);
            throw new RejectedExecutionException();
        }
    }

    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            for (int i = 0; i < threads.length; i++) {
                queue.add(SHUTDOWN_TASK);
            }
        }

        for (Thread thread : threads) {
            do {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    // do not propagate, to prevent incomplete shutdown
                }
            } while (thread.isAlive());
        }
    }
}
