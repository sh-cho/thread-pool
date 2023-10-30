package threadpool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.Test;

class ThreadPoolTest {
    @Test
    void submittedTasksAreExecuted() throws Exception {
        final Executor executor = new ThreadPool(2);
        final int count = 100;
        final CountDownLatch latch = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            final int finalI = i;
            executor.execute(() -> {
                System.err.println("Thread '" + Thread.currentThread().getName() + "' executes task " + finalI);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                latch.countDown();
            });
        }

        latch.await();
    }
}
