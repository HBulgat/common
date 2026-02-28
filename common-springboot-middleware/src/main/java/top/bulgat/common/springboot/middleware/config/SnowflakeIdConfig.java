package top.bulgat.common.springboot.middleware.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.bulgat.common.id.IdGenerator;
import top.bulgat.common.id.impl.SnowflakeIdGenerator;

/**
 * 自动配置基于 Snowflake 算法的 {@link IdGenerator} Bean。
 * <p>
 * 可通过 {@code application.yml} 进行配置：
 * <pre>
 * common:
 *   snowflake:
 *     worker-id: 1      # 0-31 工作节点 ID
 *     datacenter-id: 1  # 0-31 数据中心 ID
 * </pre>
 */
@Configuration
public class SnowflakeIdConfig {

    @Bean
    @ConfigurationProperties(prefix = "common.snowflake")
    public SnowflakeProperties snowflakeProperties() {
        return new SnowflakeProperties();
    }

    @Bean
    @ConditionalOnMissingBean(IdGenerator.class)
    public IdGenerator idGenerator(SnowflakeProperties props) {
        return new SnowflakeIdGenerator(props.getWorkerId(), props.getDatacenterId());
    }

    public static class SnowflakeProperties {
        private long workerId = 1;
        private long datacenterId = 1;

        public long getWorkerId() { return workerId; }
        public void setWorkerId(long workerId) { this.workerId = workerId; }
        public long getDatacenterId() { return datacenterId; }
        public void setDatacenterId(long datacenterId) { this.datacenterId = datacenterId; }
    }
}
