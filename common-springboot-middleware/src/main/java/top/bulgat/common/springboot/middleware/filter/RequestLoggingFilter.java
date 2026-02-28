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
 * 可选的请求/响应日志过滤器。
 * <p>
 * 日志格式：{@code [METHOD /path] STATUS | Xms | traceId=...}
 * <p>
 * 通过设置 {@code common.middleware.request-log.enabled=true} 激活 (默认：{@code false})。
 * Bean 的注册已在 {@link top.bulgat.common.springboot.middleware.MiddlewareAutoConfiguration} 中条件化完成。
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
