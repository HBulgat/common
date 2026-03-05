package top.bulgat.common.springboot.middleware.listener;

import org.slf4j.MDC;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import top.bulgat.common.springboot.middleware.component.TraceIdFilter;

import java.time.Duration;
import java.util.UUID;

/**
 * 在 Spring Boot 启动阶段即植入 traceId 的监听器。
 * 从而保证整个启动阶段打印的日志都有 traceId，方便定位应用启动失败问题。
 * 在应用完全关闭后清理。
 */
public class TraceIdApplicationRunListener implements SpringApplicationRunListener {

    private final String startupTraceId;

    public TraceIdApplicationRunListener(SpringApplication application, String[] args) {
        // 生成一个专门的表示启动期间的 traceId，带有前缀以便追溯
        startupTraceId = "BOOT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        // 第一时间注入 MDC
        MDC.put(TraceIdFilter.MDC_TRACE_KEY, startupTraceId);
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        MDC.put(TraceIdFilter.MDC_TRACE_KEY, startupTraceId);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        MDC.put(TraceIdFilter.MDC_TRACE_KEY, startupTraceId);
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        MDC.put(TraceIdFilter.MDC_TRACE_KEY, startupTraceId);
    }

    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        MDC.put(TraceIdFilter.MDC_TRACE_KEY, startupTraceId);
    }
    
    @Override
    public void ready(ConfigurableApplicationContext context, Duration timeTaken) {
        // 启动一旦完成且完全就绪，启动专属的 MDC traceId 就可以卸载了。
        // 未来的日志就由 HTTP 请求链路过滤器接手，或者是独立定时任务自行维护。
        MDC.remove(TraceIdFilter.MDC_TRACE_KEY);
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        MDC.remove(TraceIdFilter.MDC_TRACE_KEY);
    }
}
