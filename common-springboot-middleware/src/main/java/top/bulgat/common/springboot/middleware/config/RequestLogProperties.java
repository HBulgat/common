package top.bulgat.common.springboot.middleware.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 请求日志配置属性
 */
@ConfigurationProperties(prefix = "common.middleware.request-log")
@Data
public class RequestLogProperties {
    /**
     * 是否开启请求日志打印
     */
    private boolean enabled = false;
}