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
 * common-cache 的 Spring Boot 自动配置。
 *
 * <p>缓存存储选择优先级：
 * <ol>
 *   <li>如果类路径中存在 {@code StringRedisTemplate} 并且有名为该类型的 Bean → {@link RedisCacheStore}</li>
 *   <li>否则 → {@link LocalCacheStore} (Caffeine 或回退到内存实现)</li>
 * </ol>
 *
 * <p>所有的 Bean 都使用了 {@code @ConditionalOnMissingBean}，因此宿主应用可以覆盖它们。
 */
@AutoConfiguration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfiguration {

    // -----------------------------------------------------------------------
    // 缓存存储：Redis（如果 Redis 可用则首选）
    // -----------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(CacheStore.class)
    @ConditionalOnClass(StringRedisTemplate.class)
    public CacheStore redisCacheStore(StringRedisTemplate stringRedisTemplate) {
        return new RedisCacheStore(stringRedisTemplate);
    }

    // -----------------------------------------------------------------------
    // 缓存存储：本地回退策略（当没有 Redis 时使用 Caffeine 或内存）
    // -----------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(CacheStore.class)
    public CacheStore localCacheStore(CacheProperties properties) {
        // 使用 JDK ConcurrentHashMap 作为后备的内存存储
        return new LocalCacheStore();
    }

    // -----------------------------------------------------------------------
    // CacheTemplate — 用户的主要入口点
    // -----------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(CacheTemplate.class)
    public CacheTemplate cacheTemplate(CacheStore cacheStore,
                                       ObjectMapper objectMapper,
                                       CacheProperties properties) {
        return new CacheTemplate(cacheStore, objectMapper, properties);
    }
}
