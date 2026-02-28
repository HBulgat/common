package top.bulgat.common.springboot.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * {@link CacheStoreOps} 的方案 A（默认）实现。
 * 将值序列化为 JSON 并作为 Redis String 存储。
 * <p>
 * 空值标记：字面字符串 {@code "\u0000"}（包含单个 null 字符），
 * 这不可能出现在任何合法的 JSON 载荷中。
 */
public class StringCacheStoreOps<V> implements CacheStoreOps<V> {

    static final String NULL_MARKER = "\u0000";

    private final ObjectMapper objectMapper;

    public StringCacheStoreOps(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CacheHit<V> read(CacheStore store, String key, Class<V> type) {
        Optional<String> raw = store.get(key);
        if (raw.isEmpty()) {
            return CacheHit.miss(); // 真实的缓存未命中
        }
        String value = raw.get();
        if (NULL_MARKER.equals(value)) {
            return CacheHit.hit(null); // 缓存的空值标记
        }
        try {
            return CacheHit.hit(objectMapper.readValue(value, type));
        } catch (JsonProcessingException e) {
            // 损坏的条目 — 作为未命中处理以便刷新
            return CacheHit.miss();
        }
    }

    @Override
    public void write(CacheStore store, String key, V value, long ttl, TimeUnit unit) {
        try {
            store.set(key, objectMapper.writeValueAsString(value), ttl, unit);
        } catch (JsonProcessingException e) {
            throw new CacheException("Failed to serialize cache value for key: " + key, e);
        }
    }

    @Override
    public void writeNull(CacheStore store, String key, long nullTtl, TimeUnit unit) {
        store.set(key, NULL_MARKER, nullTtl, unit);
    }

    @Override
    public boolean isNullMarker(String raw) {
        return NULL_MARKER.equals(raw);
    }
}
