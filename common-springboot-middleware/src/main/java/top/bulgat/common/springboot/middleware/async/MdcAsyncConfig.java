package top.bulgat.common.springboot.middleware.async;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 配置一个任务执行器，将 MDC 上下文（如 traceId 等）
 * 从父线程传递到 {@code @Async} 工作线程。
 * <p>
 * 如果没有这个配置，异步调用会丢失 {@code traceId}，因为 MDC 是线程局部的。
 */
@EnableAsync
@Configuration
public class MdcAsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor mdcAwareTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        executor.setQueueCapacity(256);
        executor.setThreadNamePrefix("async-");
        executor.setTaskDecorator(mdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    private TaskDecorator mdcTaskDecorator() {
        return task -> {
            // 移交前捕获父线程的 MDC
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    task.run();
                } finally {
                    MDC.clear();
                }
            };
        };
    }
}
