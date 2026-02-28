package top.bulgat.common.id;

import org.junit.jupiter.api.Test;
import top.bulgat.common.base.id.IdGenerator;
import top.bulgat.common.base.id.impl.SnowflakeIdGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeIdGeneratorTest {

    @Test
    void testImplementsIdGenerator() {
        IdGenerator generator = new SnowflakeIdGenerator(1, 1);
        long id = generator.nextId();
        assertTrue(id > 0);
    }

    @Test
    void testGenerateUniqueIds() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        Set<Long> ids = new HashSet<>();
        int count = 10000;
        for (int i = 0; i < count; i++) {
            ids.add(generator.nextId());
        }
        assertEquals(count, ids.size());
    }

    @Test
    void testIdIncreasing() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator();
        long prev = generator.nextId();
        for (int i = 0; i < 1000; i++) {
            long current = generator.nextId();
            assertTrue(current > prev);
            prev = current;
        }
    }

    @Test
    void testMultiThreadSafety() throws InterruptedException {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        int threadCount = 10;
        int perThread = 1000;
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < perThread; i++) {
                        ids.add(generator.nextId());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
        assertEquals(threadCount * perThread, ids.size());
    }

    @Test
    void testNextIdStr() {
        IdGenerator generator = new SnowflakeIdGenerator();
        String idStr = generator.nextIdStr();
        assertNotNull(idStr);
        assertFalse(idStr.isBlank());
        assertDoesNotThrow(() -> Long.parseLong(idStr));
    }

    @Test
    void testInvalidWorkerId() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(32, 0));
    }

    @Test
    void testInvalidDatacenterId() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(0, -1));
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(0, 32));
    }
}
