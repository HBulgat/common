package top.bulgat.common.base.thread;

/**
 * traceId 的线程上下文持有者。
 * <p>
 * 在 {@link ThreadLocal} 中存储 traceId，用于跨方法调用的请求追踪。
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
