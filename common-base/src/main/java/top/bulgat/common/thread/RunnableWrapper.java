package top.bulgat.common.thread;

/**
 * Runnable wrapper that propagates logId across thread boundaries.
 * <p>
 * Captures the logId from the submitting thread via {@link ThreadContext}
 * and restores it in the executing thread.
 *
 * <pre>
 * ThreadContext.setLogId(snowflake.nextId());
 * executor.submit(RunnableWrapper.of(myRunnable));
 * </pre>
 */
public class RunnableWrapper implements Runnable {

    private final Runnable runnable;

    private final Long logId;

    public RunnableWrapper(Runnable runnable) {
        this.runnable = runnable;
        this.logId = ThreadContext.getLogId();
    }

    public static RunnableWrapper of(Runnable runnable) {
        return new RunnableWrapper(runnable);
    }

    @Override
    public void run() {
        ThreadContext.setLogId(logId);
        try {
            runnable.run();
        } finally {
            ThreadContext.clear();
        }
    }
}
