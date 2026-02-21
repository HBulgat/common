package top.bulgat.common.springboot.cache;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Abstraction over the cache storage backend.
 * <p>
 * Implementations:
 * <ul>
 *   <li>{@code RedisCacheStore} — backed by {@code StringRedisTemplate}</li>
 *   <li>{@code LocalCacheStore}  — backed by Caffeine + Java collections</li>
 * </ul>
 *
 * <p><b>String operations</b> are used by {@link CacheTemplate} internally for
 * object/JSON caching. <b>Hash, List, Set, ZSet operations</b> are exposed as
 * raw primitives for direct use when Redis-native semantics are required
 * (e.g. atomic counters, ranked sets, queues). They are NOT used by
 * {@code CacheTemplate}'s strategy methods.
 */
public interface CacheStore {

    // ====================================================================
    // String — used by CacheTemplate strategy methods
    // ====================================================================

    void set(String key, String value, long ttl, TimeUnit unit);

    void setForever(String key, String value);

    Optional<String> get(String key);

    void delete(String key);

    boolean hasKey(String key);

    /**
     * Update TTL for an existing key. Returns remaining TTL in seconds,
     * or -1 if key does not exist.
     */
    long expire(String key, long ttl, TimeUnit unit);

    /**
     * Batch get (MGET). Returns a map of key → value for all found keys.
     * Missing keys are absent from the result map.
     */
    Map<String, Optional<String>> multiGet(List<String> keys);

    // ====================================================================
    // Hash — direct use only (not used by CacheTemplate)
    // ====================================================================

    void hSet(String key, String field, String value);

    void hSetAll(String key, Map<String, String> entries);

    Optional<String> hGet(String key, String field);

    Map<String, String> hGetAll(String key);

    void hDel(String key, String... fields);

    Long hIncrBy(String key, String field, long delta);

    // ====================================================================
    // List — direct use only
    // ====================================================================

    Long lPush(String key, String... values);

    Long rPush(String key, String... values);

    List<String> lRange(String key, long start, long end);

    String lPop(String key);

    String rPop(String key);

    Long lLen(String key);

    // ====================================================================
    // Set — direct use only
    // ====================================================================

    Long sAdd(String key, String... values);

    Set<String> sMembers(String key);

    Boolean sIsMember(String key, String value);

    Long sRem(String key, String... values);

    // ====================================================================
    // ZSet (Sorted Set) — direct use only
    // ====================================================================

    Boolean zAdd(String key, String value, double score);

    Set<String> zRange(String key, long start, long end);

    Set<String> zRevRange(String key, long start, long end);

    Double zScore(String key, String value);

    Long zRank(String key, String value);

    Long zRem(String key, String... values);

    Double zIncrBy(String key, String value, double delta);

    Set<String> zRangeByScore(String key, double min, double max);
}
