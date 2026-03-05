package top.bulgat.common.springboot.middleware.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import top.bulgat.common.base.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;

@Aspect
@Order(1)
public class RequestLogAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);

    private final ObjectMapper objectMapper;

    public RequestLogAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String fullUri = query != null ? uri + "?" + query : uri;

        // 获取并尝试序列化请求参数
        String requestBody = "";
        if (hasRequestBody(method)) {
            Object[] args = joinPoint.getArgs();
            try {
                Object[] serializableArgs = filterArgs(args);
                requestBody = JsonUtils.toJson(serializableArgs);
                log.info("[{} {}] begin | requestBody={}", method, fullUri, requestBody);
            } catch (Exception e) {
                log.info("[{} {}] begin | requestBody=[Serialize Error: {}]", method, fullUri, e.getMessage());
            }
        } else {
            log.info("[{} {}] begin", method, fullUri);
        }

        StopWatch sw = new StopWatch();
        sw.start();

        Object result = null;
        Throwable exception = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            exception = t;
            throw t;
        } finally {
            sw.stop();
            HttpServletResponse response = attributes.getResponse();
            int status = response != null ? response.getStatus() : 200;

            String responseBody = "";
            if (exception != null) {
                responseBody = "[Exception: " + exception.getClass().getSimpleName() + " - " + exception.getMessage() + "]";
                // AOP 切面在全局异常处理器之前捕获到异常，此时 HTTP 状态码通常还未被修改，为了日志明确标识，此处显示保留异常信息
            } else {
                try {
                    if (result != null) {
                        if (result instanceof String) {
                            responseBody = (String) result;
                        } else {
                            responseBody = JsonUtils.toJson(result);
                        }
                    }
                } catch (Exception e) {
                    responseBody = "[Serialize Error: " + e.getMessage() + "]";
                }
            }

            log.info("[{} {}] end | status={} | {}ms | responseBody={}",
                    method, fullUri,
                    status,
                    sw.getTotalTimeMillis(),
                    responseBody);
        }
    }

    private boolean hasRequestBody(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

    private Object[] filterArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return new Object[0];
        }
        List<Object> filtered = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest ||
                    arg instanceof HttpServletResponse ||
                    arg instanceof MultipartFile ||
                    arg instanceof MultipartFile[]) {
                continue;
            }
            filtered.add(arg);
        }
        return filtered.toArray();
    }
}
