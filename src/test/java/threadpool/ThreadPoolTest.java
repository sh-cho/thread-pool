package threadpool;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

class ThreadPoolTest {
    @Test
    void submittedTasksAreExecuted() throws Exception {
        final ThreadPool executor = new ThreadPool(100, Duration.ofSeconds(1));
        final int count = 100;
        final CountDownLatch latch = new CountDownLatch(count);

        try {
            for (int i = 0; i < count; i++) {
                final int finalI = i;
                executor.execute(() -> {
                    System.err.println("Thread '" + Thread.currentThread().getName() + "' executes task " + finalI);
//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }

                    latch.countDown();
                });
            }

//            latch.await();
        } finally {
            executor.shutdown();
        }
    }
}
