package top.bulgat.common.springboot.cache.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.bulgat.common.springboot.cache.CacheStore;
import top.bulgat.common.springboot.cache.CacheTemplate;
import top.bulgat.common.springboot.cache.store.LocalCacheStore;
import top.bulgat.common.springboot.cache.store.RedisCacheStore;

/**
 * Spring Boot auto-configuration for common-cache.
 *
 * <p>Store selection priority:
 * <ol>
 *   <li>If {@code StringRedisTemplate} is on the classpath and a bean is present → {@link RedisCacheStore}</li>
 *   <li>Otherwise → {@link LocalCacheStore} (Caffeine or fallback in-memory)</li>
 * </ol>
 *
 * <p>All beans use {@code @ConditionalOnMissingBean} so host applications can override.
 */
@AutoConfiguration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfiguration {

    // -----------------------------------------------------------------------
    // Store: Redis (preferred when Redis is available)
    // -----------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(CacheStore.class)
    @ConditionalOnClass(StringRedisTemplate.class)
    public CacheStore redisCacheStore(StringRedisTemplate stringRedisTemplate) {
        return new RedisCacheStore(stringRedisTemplate);
    }

    // -----------------------------------------------------------------------
    // Store: Local fallback (Caffeine / in-memory when Redis is absent)
    // -----------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(CacheStore.class)
    public CacheStore localCacheStore(CacheProperties properties) {
        // Fallback in-memory store using JDK ConcurrentHashMap
        return new LocalCacheStore();
    }

    // -----------------------------------------------------------------------
    // CacheTemplate — the main entry point for users
    // -----------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(CacheTemplate.class)
    public CacheTemplate cacheTemplate(CacheStore cacheStore,
                                       ObjectMapper objectMapper,
                                       CacheProperties properties) {
        return new CacheTemplate(cacheStore, objectMapper, properties);
    }
}
