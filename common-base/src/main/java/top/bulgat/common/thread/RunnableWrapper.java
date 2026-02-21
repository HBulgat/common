package top.bulgat.common.thread;

/**
 * Runnable wrapper that propagates traceId across thread boundaries.
 * <p>
 * Captures the traceId from the submitting thread via {@link ThreadContext}
 * and restores it in the executing thread.
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
