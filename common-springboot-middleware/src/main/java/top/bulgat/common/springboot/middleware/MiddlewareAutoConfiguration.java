package top.bulgat.common.springboot.middleware;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import top.bulgat.common.springboot.middleware.async.MdcAsyncConfig;
import top.bulgat.common.springboot.middleware.config.JacksonConfig;
import top.bulgat.common.springboot.middleware.config.SnowflakeIdConfig;
import top.bulgat.common.springboot.middleware.exception.GlobalExceptionHandler;
import top.bulgat.common.springboot.middleware.filter.TraceIdFilter;
import top.bulgat.common.springboot.middleware.filter.RequestLoggingFilter;

/**
 * Spring Boot 3.x auto-configuration entry point.
 * <p>
 * Automatically imported via {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.
 * All beans use {@code @ConditionalOnMissingBean} where appropriate so the host application
 * can override any part of the configuration.
 */
@AutoConfiguration
@Import({
        JacksonConfig.class,
        SnowflakeIdConfig.class,
        MdcAsyncConfig.class,
        GlobalExceptionHandler.class,
})
public class MiddlewareAutoConfiguration {

    // ---- LogId Filter (always active, highest priority) ----

    @Bean
    public FilterRegistrationBean<TraceIdFilter> logIdFilter() {
        FilterRegistrationBean<TraceIdFilter> reg = new FilterRegistrationBean<>(new TraceIdFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }

    // ---- Request Logging Filter (opt-in via config property) ----

    @Bean
    @ConditionalOnProperty(
            prefix = "common.middleware.request-log",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = false
    )
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> reg = new FilterRegistrationBean<>(new RequestLoggingFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return reg;
    }
}
