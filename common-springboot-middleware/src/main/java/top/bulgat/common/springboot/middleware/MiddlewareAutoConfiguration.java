package top.bulgat.common.springboot.middleware;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import top.bulgat.common.springboot.middleware.async.MdcAsyncConfig;
import top.bulgat.common.springboot.middleware.config.JsonConfig;
import top.bulgat.common.springboot.middleware.config.RequestLogProperties;
import top.bulgat.common.springboot.middleware.config.SnowflakeIdConfig;
import top.bulgat.common.springboot.middleware.exception.GlobalExceptionHandler;
import top.bulgat.common.springboot.middleware.component.TraceIdFilter;
import top.bulgat.common.springboot.middleware.component.RequestLogAspect;
import top.bulgat.common.base.id.IdGenerator;

/**
 * Spring Boot 3.x 自动配置入口点。
 * <p>
 * 通过
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * 自动导入。
 * 所有的 Bean 在合适的地方都使用了 {@code @ConditionalOnMissingBean}，因此宿主应用
 * 可以覆盖配置的任何部分。
 */
@AutoConfiguration
@EnableConfigurationProperties(RequestLogProperties.class)
@Import({
        JsonConfig.class,
        SnowflakeIdConfig.class,
        MdcAsyncConfig.class,
        GlobalExceptionHandler.class,
})
public class MiddlewareAutoConfiguration {

    // ---- LogId 过滤器 (始终激活，最高优先级) ----

    @Bean
    public FilterRegistrationBean<TraceIdFilter> logIdFilter(IdGenerator idGenerator) {
        FilterRegistrationBean<TraceIdFilter> reg = new FilterRegistrationBean<>(new TraceIdFilter(idGenerator));
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }

    // ---- 请求日志切面 (通过配置属性开启) ----

    @Bean
    @ConditionalOnProperty(prefix = "common.middleware.request-log", name = "enabled", havingValue = "true")
    public RequestLogAspect requestLogAspect(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return new RequestLogAspect(objectMapper);
    }
}
