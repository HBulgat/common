package top.bulgat.common.time;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 高性能系统时钟。
 * <p>
 * 以可配置的精度缓存 {@link System#currentTimeMillis()} 以避免
 * 频繁的系统调用。适用于高吞吐量场景（例如 ID 生成，日志记录）。
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
     * 获取单例的毫秒级精度时钟实例。
     */
    public static SystemClock millisClock() {
        return MILLIS_CLOCK;
    }

    /**
     * 获取缓存的当前时间的毫秒数。
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
