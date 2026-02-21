package top.bulgat.common.springboot.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Plan A (default) implementation of {@link CacheStoreOps}.
 * Serializes values to JSON and stores them as Redis Strings.
 * <p>
 * Null marker: the literal string {@code "\u0000"} (a single null char),
 * which cannot appear in any valid JSON payload.
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
            return CacheHit.miss(); // true cache miss
        }
        String value = raw.get();
        if (NULL_MARKER.equals(value)) {
            return CacheHit.hit(null); // cached null marker
        }
        try {
            return CacheHit.hit(objectMapper.readValue(value, type));
        } catch (JsonProcessingException e) {
            // corrupted entry — treat as miss so it gets refreshed
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
