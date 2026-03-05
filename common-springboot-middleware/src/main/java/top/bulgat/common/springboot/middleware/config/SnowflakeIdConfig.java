package top.bulgat.common.springboot.middleware.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.bulgat.common.base.id.IdGenerator;
import top.bulgat.common.base.id.impl.SnowflakeIdGenerator;

/**
 * 自动配置基于 Snowflake 算法的 {@link IdGenerator} Bean。
 * <p>
 * 可通过 {@code application.yml} 进行配置：
 * <pre>
 * common:
 *   middleware:
 *      snowflake:
 *          worker-id: 1      # 0-31 工作节点 ID
 *          datacenter-id: 1  # 0-31 数据中心 ID
 * </pre>
 */
@Configuration
public class SnowflakeIdConfig {

    @Bean
    @ConfigurationProperties(prefix = "common.middleware.snowflake")
    public SnowflakeProperties snowflakeProperties() {
        return new SnowflakeProperties();
    }

    @Bean
    @ConditionalOnMissingBean(IdGenerator.class)
    public IdGenerator idGenerator(SnowflakeProperties props) {
        return new SnowflakeIdGenerator(props.getWorkerId(), props.getDatacenterId());
    }

    @Data
    public static class SnowflakeProperties {
        private long workerId = 1;
        private long datacenterId = 1;
    }
}
