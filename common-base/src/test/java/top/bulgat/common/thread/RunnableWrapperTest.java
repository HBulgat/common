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
    void testTraceIdPropagation() throws InterruptedException {
        ThreadContext.setTraceId("abc123");

        AtomicReference<String> captured = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(RunnableWrapper.of(() -> {
            captured.set(ThreadContext.getTraceId());
            latch.countDown();
        }));
        latch.await();
        executor.shutdown();

        assertEquals("abc123", captured.get());
    }

    @Test
    void testContextClearedAfterRun() throws InterruptedException {
        ThreadContext.setTraceId("to-be-cleared");

        AtomicReference<String> afterRun = new AtomicReference<>("");
        CountDownLatch latch = new CountDownLatch(1);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(RunnableWrapper.of(() -> {}));
        executor.submit(() -> {
            afterRun.set(ThreadContext.getTraceId());
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
