package top.bulgat.common.thread;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class RunnableWrapperTest {

    @AfterEach
    void cleanup() {
        ThreadContext.clear();
    }

    @Test
    void testLogIdPropagation() throws InterruptedException {
        ThreadContext.setLogId(123456L);

        AtomicReference<Long> captured = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(RunnableWrapper.of(() -> {
            captured.set(ThreadContext.getLogId());
            latch.countDown();
        }));
        latch.await();
        executor.shutdown();

        assertEquals(123456L, captured.get());
    }

    @Test
    void testContextClearedAfterRun() throws InterruptedException {
        ThreadContext.setLogId(999L);

        AtomicReference<Long> afterRun = new AtomicReference<>(0L);
        CountDownLatch latch = new CountDownLatch(1);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(RunnableWrapper.of(() -> {}));
        executor.submit(() -> {
            afterRun.set(ThreadContext.getLogId());
            latch.countDown();
        });
        latch.await();
        executor.shutdown();

        assertNull(afterRun.get());
    }

    @Test
    void testOfFactory() {
        RunnableWrapper wrapper = RunnableWrapper.of(() -> {});
        assertNotNull(wrapper);
    }
}
