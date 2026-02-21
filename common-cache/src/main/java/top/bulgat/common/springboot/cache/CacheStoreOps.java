package top.bulgat.common.springboot.cache;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Extension point for Plan B: allows CacheLoader to declare how its value
 * should be stored and retrieved from the CacheStore.
 * <p>
 * Plan A only ships {@link StringCacheStoreOps}. Future implementations can
 * add HashCacheStoreOps, ZSetCacheStoreOps, etc.
 *
 * @param <V> the Java value type
 */
public interface CacheStoreOps<V> {

    /**
     * Reads the typed value from the cache store.
     * @return CacheHit containing the value (can be null), or CacheHit.miss() if not found.
     */
    CacheHit<V> read(CacheStore store, String key, Class<V> type);

    /**
     * Write a non-null value to the store with TTL.
     */
    void write(CacheStore store, String key, V value, long ttl, TimeUnit unit);

    /**
     * Write a null-marker (防穿透) with a short TTL.
     */
    void writeNull(CacheStore store, String key, long nullTtl, TimeUnit unit);

    /**
     * Whether the raw stored representation indicates a cached null.
     */
    boolean isNullMarker(String raw);
}
