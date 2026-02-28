package top.bulgat.common.springboot.cache;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 方案 B 的扩展点：允许 CacheLoader 声明它的值应该如何
 * 在 CacheStore 中存储和检索。
 * <p>
 * 方案 A 仅提供 {@link StringCacheStoreOps}。未来的实现可以
 * 添加 HashCacheStoreOps、ZSetCacheStoreOps 等。
 *
 * @param <V> Java 值的类型
 */
public interface CacheStoreOps<V> {

    /**
     * 从缓存存储中读取类型化的值。
     * @return 包含该值的 CacheHit（可能为 null），如果未找到则返回 CacheHit.miss()。
     */
    CacheHit<V> read(CacheStore store, String key, Class<V> type);

    /**
     * 将包含 TTL 的非空值写入存储。
     */
    void write(CacheStore store, String key, V value, long ttl, TimeUnit unit);

    /**
     * 写入一个带有较短 TTL 的防穿透空标记 (null-marker)。
     */
    void writeNull(CacheStore store, String key, long nullTtl, TimeUnit unit);

    /**
     * 判断原始的存储表达是否表示一个缓存的 null。
     */
    boolean isNullMarker(String raw);
}
