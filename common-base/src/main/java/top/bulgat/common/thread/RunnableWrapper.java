package top.bulgat.common.thread;

/**
 * 在线程边界间传播 traceId 的 Runnable 包装器。
 * <p>
 * 从提交线程的 {@link ThreadContext} 中捕获 traceId，
 * 并在执行线程中恢复它。
 *
 * <pre>
 * ThreadContext.setTraceId(traceId);
 * executor.submit(RunnableWrapper.of(myRunnable));
 * </pre>
 */
public class RunnableWrapper implements Runnable {

    private final Runnable runnable;

    private final String traceId;

    public RunnableWrapper(Runnable runnable) {
        this.runnable = runnable;
        this.traceId = ThreadContext.getTraceId();
    }

    public static RunnableWrapper of(Runnable runnable) {
        return new RunnableWrapper(runnable);
    }

    @Override
    public void run() {
        ThreadContext.setTraceId(traceId);
        try {
            runnable.run();
        } finally {
            ThreadContext.clear();
        }
    }
}
