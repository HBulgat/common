package top.bulgat.common.springboot.middleware.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Optional request/response logging filter.
 * <p>
 * Logs: {@code [METHOD /path] STATUS | Xms | traceId=...}
 * <p>
 * Activated by setting {@code common.middleware.request-log.enabled=true} (default: {@code false}).
 * Bean registration is done conditionally in {@link top.bulgat.common.springboot.middleware.MiddlewareAutoConfiguration}.
 */
@Order(2)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String fullUri = query != null ? uri + "?" + query : uri;

        StopWatch sw = new StopWatch();
        sw.start();
        try {
            chain.doFilter(request, response);
        } finally {
            sw.stop();
            log.info("[{} {}] {} | {}ms | traceId={}",
                    method, fullUri,
                    response.getStatus(),
                    sw.getTotalTimeMillis(),
                    response.getHeader(TraceIdFilter.TRACE_ID_HEADER));
        }
    }
}
