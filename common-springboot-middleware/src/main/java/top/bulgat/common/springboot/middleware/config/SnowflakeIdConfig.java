package top.bulgat.common.springboot.middleware.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.bulgat.common.id.IdGenerator;
import top.bulgat.common.id.impl.SnowflakeIdGenerator;

/**
 * Auto-configures a {@link IdGenerator} Bean backed by Snowflake algorithm.
 * <p>
 * Configurable via {@code application.yml}:
 * <pre>
 * common:
 *   snowflake:
 *     worker-id: 1      # 0-31
 *     datacenter-id: 1  # 0-31
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
