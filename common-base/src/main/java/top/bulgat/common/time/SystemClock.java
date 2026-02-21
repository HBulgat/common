package top.bulgat.common.time;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * High-performance system clock.
 * <p>
 * Caches {@link System#currentTimeMillis()} at a configurable precision to avoid
 * frequent system calls. Useful in high-throughput scenarios (e.g. ID generation, logging).
 */
public class SystemClock {

    private static final String THREAD_NAME = "system.clock";

    private final long precision;

    private static final SystemClock MILLIS_CLOCK = new SystemClock(1);

    private final AtomicLong now;

    private SystemClock(long precision) {
        this.precision = precision;
        this.now = new AtomicLong(System.currentTimeMillis());
        scheduleClockUpdate();
    }

    /**
     * Get the singleton millisecond-precision clock instance.
     */
    public static SystemClock millisClock() {
        return MILLIS_CLOCK;
    }

    /**
     * Get the cached current time in milliseconds.
     */
    public long now() {
        return now.get();
    }

    private void scheduleClockUpdate() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, THREAD_NAME);
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(
                () -> now.set(System.currentTimeMillis()),
                precision,
                precision,
                TimeUnit.MILLISECONDS
        );
    }
}
