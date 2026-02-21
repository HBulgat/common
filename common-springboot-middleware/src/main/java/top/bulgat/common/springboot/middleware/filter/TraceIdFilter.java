package top.bulgat.common.springboot.middleware.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import top.bulgat.common.thread.ThreadContext;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that assigns a unique {@code traceId} to every HTTP request.
 * <ul>
 *   <li>Stores the traceId in {@link MDC} as {@code traceId} (available in Logback pattern as {@code %X{traceId}})</li>
 *   <li>Stores in {@link ThreadContext} for application-level access</li>
 *   <li>Writes {@code X-Trace-Id} response header so callers can correlate logs</li>
 *   <li>Cleans up MDC/ThreadContext in {@code finally} to prevent memory leaks</li>
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
        // Honour existing traceId from upstream (e.g., gateway) if present
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
