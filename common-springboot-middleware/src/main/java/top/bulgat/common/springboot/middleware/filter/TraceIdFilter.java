package top.bulgat.common.springboot.middleware.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import top.bulgat.common.base.thread.ThreadContext;

import java.io.IOException;
import java.util.UUID;

/**
 * 为每个 HTTP 请求分配唯一的 {@code traceId} 的 Servlet 过滤器。
 * <ul>
 *   <li>将 traceId 存入 {@link MDC}，键为 {@code traceId} (在 Logback 模式中可通过 {@code %X{traceId}} 获取)</li>
 *   <li>存入 {@link ThreadContext} 以供应用层访问</li>
 *   <li>向响应头写入 {@code X-Trace-Id}，方便调用方关联日志</li>
 *   <li>在 {@code finally} 块中清理 MDC/ThreadContext，防止内存泄漏</li>
 * </ul>
 */
@Order(1)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String MDC_TRACE_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // 如果存在，优先使用上游（例如网关）传入的 traceId
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(MDC_TRACE_KEY, traceId);
        ThreadContext.setTraceId(traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TRACE_KEY);
            ThreadContext.clear();
        }
    }
}
