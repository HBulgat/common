package top.bulgat.common.springboot.cache.store;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import top.bulgat.common.springboot.cache.CacheStore;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基于 Redis 和 {@link StringRedisTemplate} 的 {@link CacheStore} 实现。
 */
public class RedisCacheStore implements CacheStore {

    private final StringRedisTemplate redis;

    public RedisCacheStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // ---- String ----

    @Override
    public void set(String key, String value, long ttl, TimeUnit unit) {
        redis.opsForValue().set(key, value, ttl, unit);
    }

    @Override
    public void setForever(String key, String value) {
        redis.opsForValue().set(key, value);
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(redis.opsForValue().get(key));
    }

    @Override
    public void delete(String key) {
        redis.delete(key);
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redis.hasKey(key));
    }

    @Override
    public long expire(String key, long ttl, TimeUnit unit) {
        Boolean ok = redis.expire(key, ttl, unit);
        return Boolean.TRUE.equals(ok) ? ttl : -1L;
    }

    @Override
    public Map<String, Optional<String>> multiGet(List<String> keys) {
        List<String> values = redis.opsForValue().multiGet(keys);
        Map<String, Optional<String>> result = new LinkedHashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            String val = (values != null) ? values.get(i) : null;
            result.put(keys.get(i), Optional.ofNullable(val));
        }
        return result;
    }

    // ---- Hash ----

    @Override
    public void hSet(String key, String field, String value) {
        redis.opsForHash().put(key, field, value);
    }

    @Override
    public void hSetAll(String key, Map<String, String> entries) {
        redis.opsForHash().putAll(key, entries);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<String> hGet(String key, String field) {
        Object val = redis.opsForHash().get(key, field);
        return Optional.ofNullable((String) val);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> hGetAll(String key) {
        Map<Object, Object> raw = redis.opsForHash().entries(key);
        Map<String, String> result = new LinkedHashMap<>();
        raw.forEach((k, v) -> result.put((String) k, (String) v));
        return result;
    }

    @Override
    public void hDel(String key, String... fields) {
        redis.opsForHash().delete(key, (Object[]) fields);
    }

    @Override
    public Long hIncrBy(String key, String field, long delta) {
        return redis.opsForHash().increment(key, field, delta);
    }

    // ---- List ----

    @Override
    public Long lPush(String key, String... values) {
        return redis.opsForList().leftPushAll(key, values);
    }

    @Override
    public Long rPush(String key, String... values) {
        return redis.opsForList().rightPushAll(key, values);
    }

    @Override
    public List<String> lRange(String key, long start, long end) {
        List<String> result = redis.opsForList().range(key, start, end);
        return result == null ? Collections.emptyList() : result;
    }

    @Override
    public String lPop(String key) {
        return redis.opsForList().leftPop(key);
    }

    @Override
    public String rPop(String key) {
        return redis.opsForList().rightPop(key);
    }

    @Override
    public Long lLen(String key) {
        return redis.opsForList().size(key);
    }

    // ---- Set ----

    @Override
    public Long sAdd(String key, String... values) {
        return redis.opsForSet().add(key, values);
    }

    @Override
    public Set<String> sMembers(String key) {
        Set<String> result = redis.opsForSet().members(key);
        return result == null ? Collections.emptySet() : result;
    }

    @Override
    public Boolean sIsMember(String key, String value) {
        return redis.opsForSet().isMember(key, value);
    }

    @Override
    public Long sRem(String key, String... values) {
        return redis.opsForSet().remove(key, (Object[]) values);
    }

    // ---- ZSet ----

    @Override
    public Boolean zAdd(String key, String value, double score) {
        return redis.opsForZSet().add(key, value, score);
    }

    @Override
    public Set<String> zRange(String key, long start, long end) {
        Set<String> result = redis.opsForZSet().range(key, start, end);
        return result == null ? Collections.emptySet() : result;
    }

    @Override
    public Set<String> zRevRange(String key, long start, long end) {
        Set<String> result = redis.opsForZSet().reverseRange(key, start, end);
        return result == null ? Collections.emptySet() : result;
    }

    @Override
    public Double zScore(String key, String value) {
        return redis.opsForZSet().score(key, value);
    }

    @Override
    public Long zRank(String key, String value) {
        return redis.opsForZSet().rank(key, value);
    }

    @Override
    public Long zRem(String key, String... values) {
        return redis.opsForZSet().remove(key, (Object[]) values);
    }

    @Override
    public Double zIncrBy(String key, String value, double delta) {
        return redis.opsForZSet().incrementScore(key, value, delta);
    }

    @Override
    public Set<String> zRangeByScore(String key, double min, double max) {
        Set<String> result = redis.opsForZSet().rangeByScore(key, min, max);
        return result == null ? Collections.emptySet() : result;
    }
}
