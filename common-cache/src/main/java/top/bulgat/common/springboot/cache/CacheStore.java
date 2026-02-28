package top.bulgat.common.springboot.cache;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存存储后端的抽象层。
 * <p>
 * 实现类:
 * <ul>
 *   <li>{@code RedisCacheStore} — 基于 {@code StringRedisTemplate}</li>
 *   <li>{@code LocalCacheStore} — 基于 Caffeine + Java 集合</li>
 * </ul>
 *
 * <p><b>String 操作</b> 主要被 {@link CacheTemplate} 内部用于对象/JSON 缓存。
 * <b>Hash, List, Set, ZSet 操作</b> 作为原生原语暴露，在需要 Redis 原生语义时直接使用
 * （例如原子计数器、排名集、队列）。它们<b>不被</b> {@code CacheTemplate} 的策略方法所使用。
 */
public interface CacheStore {

    // ====================================================================
    // String (字符串)操作 — 被 CacheTemplate 策略方法内部使用
    // ====================================================================

    void set(String key, String value, long ttl, TimeUnit unit);

    void setForever(String key, String value);

    Optional<String> get(String key);

    void delete(String key);

    boolean hasKey(String key);

    /**
     * 更新现有键的 TTL（有效时间）。返回剩余的 TTL 秒数，
     * 如果键不存在则返回 -1。
     */
    long expire(String key, long ttl, TimeUnit unit);

    /**
     * 批量获取 (MGET)。返回所有找到的键的值的字典（key → value）。
     * 结果字典中不包含未命中的键。
     */
    Map<String, Optional<String>> multiGet(List<String> keys);

    // ====================================================================
    // Hash (哈希)操作 — 仅供直接使用 (不被 CacheTemplate 使用)
    // ====================================================================

    void hSet(String key, String field, String value);

    void hSetAll(String key, Map<String, String> entries);

    Optional<String> hGet(String key, String field);

    Map<String, String> hGetAll(String key);

    void hDel(String key, String... fields);

    Long hIncrBy(String key, String field, long delta);

    // ====================================================================
    // List (列表)操作 — 仅供直接使用
    // ====================================================================

    Long lPush(String key, String... values);

    Long rPush(String key, String... values);

    List<String> lRange(String key, long start, long end);

    String lPop(String key);

    String rPop(String key);

    Long lLen(String key);

    // ====================================================================
    // Set (集合)操作 — 仅供直接使用
    // ====================================================================

    Long sAdd(String key, String... values);

    Set<String> sMembers(String key);

    Boolean sIsMember(String key, String value);

    Long sRem(String key, String... values);

    // ====================================================================
    // ZSet (有序集合)操作 — 仅供直接使用
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
