package top.bulgat.common.thread;

/**
 * Thread context holder for traceId.
 * <p>
 * Stores a traceId in {@link ThreadLocal} for request tracing across method calls.
 */
public final class ThreadContext {

    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    private ThreadContext() {
    }

    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    public static String getTraceId() {
        return TRACE_ID.get();
    }

    public static void clear() {
        TRACE_ID.remove();
    }
}
