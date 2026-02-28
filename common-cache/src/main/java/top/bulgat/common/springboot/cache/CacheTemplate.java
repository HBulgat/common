package top.bulgat.common.springboot.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import top.bulgat.common.springboot.cache.bloom.BloomFilterOps;
import top.bulgat.common.springboot.cache.config.CacheProperties;

/**
 * CacheTemplate 是基于 {@link CacheStore} 的高级策略编排门面。
 * 业务代码无需直接接触底层的 CacheStore（如 Redis/本地缓存），
 * 只需要实现 {@link CacheLoader} 并调用本类的模板方法即可。
 *
 * <p><b>核心策略与解决的问题：</b>
 * <ul>
 *   <li>{@link #getWithPassThroughNullMarker} — <b>防缓存穿透</b>: 针对不存在的 Key 缓存空值标记，阻断恶意请求打到 DB</li>
 *   <li>{@link #getWithLogicalExpiry} — <b>防缓存击穿</b>: Key 永不真正从缓存剔除（无 TTL），过期时快速返回旧值，同时异步安全重构缓存</li>
 *   <li>{@link #multiGetWithPassThroughNullMarker} — <b>批量加载优化</b>: MGET 批量读取缓存，缺失的 Key 统一调用 multiLoad() 减少网络 I/O</li>
 *   <li><b>TTL 随机抖动 (Jitter)</b> — <b>防缓存雪崩</b>: 对所有主动写入的 TTL 附加随机浮动因子，彻底打散大批量缓存同时过期的风险</li>
 * </ul>
 */
public class CacheTemplate {

    private static final Logger log = LoggerFactory.getLogger(CacheTemplate.class);

    private final CacheStore store;
    private final ObjectMapper objectMapper;
    private final CacheProperties properties;
    /** 用于后台逻辑过期重建的异步线程池。 */
    private final ExecutorService rebuildExecutor;

    public CacheTemplate(CacheStore store, ObjectMapper objectMapper, CacheProperties properties) {
        this.store = store;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.rebuildExecutor = Executors.newFixedThreadPool(properties.getRebuildExecutorSize(),
                r -> {
                    Thread t = new Thread(r, "cache-rebuild");
                    t.setDaemon(true);
                    return t;
                });
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * 【防缓存穿透】基于空值标记 (Null-Marker) 的读取策略。
     * <p>执行流程：
     * <ol>
     *   <li>通过 {@code loader.storeOps().read()} 尝试读取混存</li>
     *   <li>如果命中 → 直接返回由于反序列化得到的目标对象（如果命中防穿透空标记，则安全返回 null）</li>
     *   <li>如果真实未命中 (Miss) → 调用 {@code loader.load(id)} 同步查询数据库</li>
     *   <li>若数据库无相关数据 → 在缓存中写入短暂有效期的空值标记 (Null-Marker) 保护下游，返回 null</li>
     *   <li>若数据库成功返回 → 基于随机抖动后的 TTL 写入缓存，返回业务数据</li>
     * </ol>
     */
    public <ID, V> V getWithPassThroughNullMarker(ID id, Class<V> type, CacheLoader<ID, V> loader) {
        CacheStoreOps<V> ops = resolveOps(loader);
        String key = loader.key(id);

        CacheHit<V> cached = ops.read(store, key, type);
        if (cached.isHit()) {
            return cached.getValue(); // may be null (null-marker hit)
        }

        // True miss — load from source
        V value = loader.load(id);
        if (value == null) {
            ops.writeNull(store, key, properties.getNullValueTtl(), TimeUnit.SECONDS);
            return null;
        }
        ops.write(store, key, value, jitter(loader.ttl(), loader.ttlUnit()), TimeUnit.MILLISECONDS);
        return value;
    }

    /**
     * 【后台数据预热】（在使用 {@link #getWithLogicalExpiry} 前必须调用）。
     * 主动将业务数据封装为包含逻辑过期的 {@link CacheData} 写入缓存，且在底层存储（如 Redis）中不设置真实 TTL。
     */
    public <ID, V> void warmUp(ID id, CacheLoader<ID, V> loader) {
        String key = loader.key(id);
        V value = loader.load(id);
        writeWithLogicalExpiry(key, value, loader.ttl(), loader.ttlUnit());
    }

    /**
     * 【防缓存击穿】基于逻辑过期 (Logical Expiry) 的读取策略。
     * <p>执行流程：
     * <ol>
     *   <li>从缓存中读取 {@link CacheData}（键必须已被预热）</li>
     *   <li>如果未过期 → 立即返回内部包装的数据</li>
     *   <li>如果已过期 → 提交异步重建任务；立即返回旧数据</li>
     * </ol>
     */
    public <ID, V> V getWithLogicalExpiry(ID id, Class<V> type, CacheLoader<ID, V> loader) {
        String key = loader.key(id);
        Optional<String> raw = store.get(key);

        if (raw.isEmpty()) {
            log.warn("[CacheTemplate] Key '{}' not found. Did you call warmUp() first?", key);
            return null;
        }

        CacheData<V> cacheData = deserializeWrapped(raw.get(), type);
        if (cacheData == null) return null;

        V value = cacheData.getData();
        if (!cacheData.isExpired()) {
            return value; // still valid — fast path
        }

        // 逻辑已过期: 触发异步重建并返回旧数据
        rebuildExecutor.submit(() -> rebuildLogicalExpiry(id, loader));
        return value;
    }

    /**
     * 【批量防穿透】批量获取并自带防穿透空值标记功能。
     * <ol>
     *   <li>通过 MGET 批量读取底层缓存 (极大减少网络 RTT)</li>
     *   <li>将结果划分为命中 (Hits) 和完全未命中 (Misses) 两组</li>
     *   <li>针对所有未命中的 ID，统一调用 {@code loader.multiLoad(missedIds)} 从数据库一次性拉取</li>
     *   <li>将数据库返回的有效数据带上（随机抖动）TTL 写入缓存，对依然缺失的数据写入 Null-Marker 短暂截断后续无效请求</li>
     * </ol>
     */
    public <ID, V> Map<ID, V> multiGetWithPassThroughNullMarker(List<ID> ids, Class<V> type, CacheLoader<ID, V> loader) {
        // Build key list preserving order
        List<String> keys = new ArrayList<>(ids.size());
        for (ID id : ids) keys.add(loader.key(id));

        Map<String, Optional<String>> rawResults = store.multiGet(keys);

        Map<ID, V> result = new LinkedHashMap<>();
        List<ID> missedIds = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            ID id = ids.get(i);
            String key = keys.get(i);
            Optional<String> raw = rawResults.getOrDefault(key, Optional.empty());

            if (raw.isEmpty()) {
                // True miss
                missedIds.add(id);
            } else {
                String val = raw.get();
                if (!StringCacheStoreOps.NULL_MARKER.equals(val)) {
                    V v = deserialize(val, type);
                    if (v != null) result.put(id, v);
                }
                // else: cached null-marker → skip (do not add to result or missedIds)
            }
        }

        if (!missedIds.isEmpty()) {
            Map<ID, V> loaded = loader.multiLoad(missedIds);
            for (ID id : missedIds) {
                String key = loader.key(id);
                V v = loaded.get(id);
                if (v == null) {
                    store.set(key, StringCacheStoreOps.NULL_MARKER,
                            properties.getNullValueTtl(), TimeUnit.SECONDS);
                } else {
                    result.put(id, v);
                    store.set(key, serialize(v),
                            jitter(loader.ttl(), loader.ttlUnit()), TimeUnit.MILLISECONDS);
                }
            }
        }

        return result;
    }

    /**
     * 【方案 B 防穿透】基于布隆过滤器的单条读取策略。
     * <ol>
     *   <li>先查询布隆过滤器，如果确定不存在（返回 false），则直接拦截返回 null</li>
     *   <li>如果可能存在（返回 true），则执行与 getWithPassThroughNullMarker 相似的缓存/DB查询逻辑</li>
     * </ol>
     */
    public <ID, V> V getWithPassThroughBloomFilter(ID id, Class<V> type, CacheLoader<ID, V> loader, BloomFilterOps<ID> bloomFilter) {
        if (!bloomFilter.mightContain(id)) {
            return null; // 被布隆过滤器拦截，直接返回空，保护 DB 层
        }
        return getWithPassThroughNullMarker(id, type, loader);
    }

    /**
     * 【方案 B 批量防穿透】基于布隆过滤器的批量读取策略。
     */
    public <ID, V> Map<ID, V> multiGetWithPassThroughBloomFilter(List<ID> ids, Class<V> type, CacheLoader<ID, V> loader, BloomFilterOps<ID> bloomFilter) {
        List<ID> possibleIds = new ArrayList<>();
        for (ID id : ids) {
            if (bloomFilter.mightContain(id)) {
                possibleIds.add(id);
            }
        }
        if (possibleIds.isEmpty()) return new LinkedHashMap<>();
        return multiGetWithPassThroughNullMarker(possibleIds, type, loader);
    }

    /**
     * 【防击穿互斥锁】（暂未原生提供分布式锁基础设施，抛出 Unsupported 异常）。
     */
    public <ID, V> V getWithMutex(ID id, Class<V> type, CacheLoader<ID, V> loader) {
        throw new UnsupportedOperationException("Mutex lock cache strategy is not yet implemented natively in this version.");
    }

    /**
     * 【批量防击穿】批量逻辑过期查询。
     * 对于所有未过期的返回有效值；对于已过期的立刻返回旧数据，并统一提交一个异步批量重构任务。
     */
    public <ID, V> Map<ID, V> multiGetWithLogicalExpiry(List<ID> ids, Class<V> type, CacheLoader<ID, V> loader) {
        List<String> keys = new ArrayList<>(ids.size());
        for (ID id : ids) keys.add(loader.key(id));

        Map<String, Optional<String>> rawResults = store.multiGet(keys);
        Map<ID, V> result = new LinkedHashMap<>();
        List<ID> logicallyExpiredIds = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            ID id = ids.get(i);
            String key = keys.get(i);
            Optional<String> raw = rawResults.getOrDefault(key, Optional.empty());

            if (raw.isEmpty()) {
                log.warn("[CacheTemplate] Key '{}' not found in multiGetWithLogicalExpiry. Did you call multiWarmUp()?", key);
                continue;
            }

            CacheData<V> cacheData = deserializeWrapped(raw.get(), type);
            if (cacheData == null) continue;

            result.put(id, cacheData.getData());
            if (cacheData.isExpired()) {
                logicallyExpiredIds.add(id);
            }
        }

        if (!logicallyExpiredIds.isEmpty()) {
            rebuildExecutor.submit(() -> rebuildLogicalExpiryMulti(logicallyExpiredIds, loader));
        }
        return result;
    }

    /**
     * 【批量预热】对多个 ID 批量预热逻辑过期数据。
     */
    public <ID, V> void multiWarmUp(List<ID> ids, CacheLoader<ID, V> loader) {
        Map<ID, V> loaded = loader.multiLoad(ids);
        for (Map.Entry<ID, V> entry : loaded.entrySet()) {
            writeWithLogicalExpiry(loader.key(entry.getKey()), entry.getValue(), loader.ttl(), loader.ttlUnit());
        }
    }

    /**
     * 【批量主动写入】无感覆盖写入缓存多个元素（带 TTL 抖动）。
     */
    public <ID, V> void multiPut(Map<ID, V> idValueMap, CacheLoader<ID, V> loader) {
        CacheStoreOps<V> ops = resolveOps(loader);
        for (Map.Entry<ID, V> entry : idValueMap.entrySet()) {
            ops.write(store, loader.key(entry.getKey()), entry.getValue(), 
                    jitter(loader.ttl(), loader.ttlUnit()), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 【主动写入】显式将一个值存入缓存，强制覆盖。
     * 写入时会自动应用 TTL 防雪崩随机抖动机制（基于配置的 jitterFactor 浮动）。
     */
    public <ID, V> void put(ID id, V value, CacheLoader<ID, V> loader) {
        CacheStoreOps<V> ops = resolveOps(loader);
        ops.write(store, loader.key(id), value,
                jitter(loader.ttl(), loader.ttlUnit()), TimeUnit.MILLISECONDS);
    }

    /**
     * 【主动驱逐】根据 ID 生成缓存 Key，并在存储层执行删除动作。
     */
    public <ID, V> void invalidate(ID id, CacheLoader<ID, V> loader) {
        store.delete(loader.key(id));
    }

    // -----------------------------------------------------------------------
    // Expose raw store for Hash / List / Set / ZSet direct operations
    // -----------------------------------------------------------------------

    public CacheStore store() {
        return store;
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private <V> CacheStoreOps<V> resolveOps(CacheLoader<?, V> loader) {
        CacheStoreOps<V> ops = loader.storeOps();
        if (ops == null) {
            ops = new StringCacheStoreOps<>(objectMapper);
        }
        return ops;
    }

    private <V> void rebuildLogicalExpiry(Object id, CacheLoader<?, V> loader) {
        @SuppressWarnings("unchecked")
        CacheLoader<Object, V> typedLoader = (CacheLoader<Object, V>) loader;
        String key = typedLoader.key(id);

        try {
            // Double-check: another thread may have already rebuilt
            Optional<String> raw = store.get(key);
            if (raw.isPresent()) {
                CacheData<?> cacheData = deserializeWrapped(raw.get(), Object.class);
                if (cacheData != null && !cacheData.isExpired()) {
                    return;
                }
            }

            V newValue = typedLoader.load(id);
            writeWithLogicalExpiry(key, newValue, typedLoader.ttl(), typedLoader.ttlUnit());
        } catch (Throwable t) {
            System.err.println("[CacheTemplate] FATAL ERROR in rebuild: " + t.getMessage());
            t.printStackTrace();
            log.error("[CacheTemplate] Failed to rebuild logical expiry cache for key '{}': {}", key, t.getMessage(), t);
        }
    }

    private <V> void rebuildLogicalExpiryMulti(List<?> ids, CacheLoader<?, V> loader) {
        @SuppressWarnings("unchecked")
        List<Object> typedIds = (List<Object>) ids;
        @SuppressWarnings("unchecked")
        CacheLoader<Object, V> typedLoader = (CacheLoader<Object, V>) loader;

        try {
            Map<Object, V> newValues = typedLoader.multiLoad(typedIds);
            for (Map.Entry<Object, V> entry : newValues.entrySet()) {
                writeWithLogicalExpiry(typedLoader.key(entry.getKey()), entry.getValue(), typedLoader.ttl(), typedLoader.ttlUnit());
            }
        } catch (Throwable t) {
            log.error("[CacheTemplate] Failed to rebuild logical expiry cache for multi IDs: {}", t.getMessage(), t);
        }
    }

    private <V> void writeWithLogicalExpiry(String key, V value, long ttl, TimeUnit unit) {
        LocalDateTime expireAt = LocalDateTime.now().plusSeconds(unit.toSeconds(ttl));
        CacheData<V> cacheData = new CacheData<>(value, expireAt);
        store.setForever(key, serialize(cacheData));
    }

    /**
     * 【防缓存雪崩】对生成的真实的底层 TTL 应用随机抖动 (Jitter)。
     * 计算结果基于配置属性 jitter-factor，在设定 TTL 上下随机浮动，彻底打散批量数据的过期点。
     *
     * @return 最终打散后的毫秒级存活时间
     */
    private long jitter(long ttl, TimeUnit unit) {
        long ttlMs = unit.toMillis(ttl);
        double factor = properties.getJitterFactor();
        if (factor <= 0) return ttlMs;
        long jitterMs = (long) (ttlMs * factor * (Math.random() * 2 - 1));
        return Math.max(1000L, ttlMs + jitterMs); // floor at 1 second
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new CacheException("Serialization failed", e);
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        if (json == null || StringCacheStoreOps.NULL_MARKER.equals(json)) return null;
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.warn("[CacheTemplate] Failed to deserialize cached value: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 使用 Jackson TypeFactory 反序列化 {@link CacheData}{@code <V>}，
     * 以便将泛型 {@code data} 字段正确映射到类型 V。
     */
    private <V> CacheData<V> deserializeWrapped(String json, Class<V> type) {
        if (json == null) return null;
        try {
            com.fasterxml.jackson.databind.JavaType javaType = objectMapper.getTypeFactory()
                    .constructParametricType(CacheData.class, type);
            return objectMapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            log.warn("[CacheTemplate] Failed to deserialize CacheData: {}", e.getMessage());
            return null;
        }
    }
}
