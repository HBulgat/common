package top.bulgat.common.thread;

/**
 * Thread context holder for logId.
 * <p>
 * Stores a logId in {@link ThreadLocal} for request tracing across method calls.
 */
public final class ThreadContext {

    private static final ThreadLocal<Long> LOG_ID = new ThreadLocal<>();

    private ThreadContext() {
    }

    public static void setLogId(Long logId) {
        LOG_ID.set(logId);
    }

    public static Long getLogId() {
        return LOG_ID.get();
    }

    public static void clear() {
        LOG_ID.remove();
    }
}
