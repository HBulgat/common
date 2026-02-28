package top.bulgat.common.util;

import org.junit.jupiter.api.Test;
import top.bulgat.common.base.time.SystemClock;

import static org.junit.jupiter.api.Assertions.*;

class SystemClockTest {

    @Test
    void testMillisClock() {
        SystemClock clock = SystemClock.millisClock();
        assertNotNull(clock);
        long now = clock.now();
        assertTrue(now > 0);
    }

    @Test
    void testClockAccuracy() throws InterruptedException {
        SystemClock clock = SystemClock.millisClock();
        long before = System.currentTimeMillis();
        Thread.sleep(50);
        long clockNow = clock.now();
        long after = System.currentTimeMillis();
        // cached clock should be within reasonable range
        assertTrue(clockNow >= before);
        assertTrue(clockNow <= after + 10);
    }

    @Test
    void testSingleton() {
        assertSame(SystemClock.millisClock(), SystemClock.millisClock());
    }
}
