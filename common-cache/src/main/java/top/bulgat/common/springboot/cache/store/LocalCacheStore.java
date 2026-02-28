package top.bulgat.common.springboot.cache.store;

import top.bulgat.common.springboot.cache.CacheStore;

import java.util.*;
import java.util.concurrent.*;

/**
 * 仅由 JDK 并发集合支持的 {@link CacheStore} 实现。
 * 无需任何外部依赖 — 当 Redis 不可用时，作为本地/开发环境的回退实现。
 *
 * <p><b>TTL过期</b>：通过每秒运行一次的后台驱逐线程强制执行。
 * 对于 Hash / List / Set / ZSet 是没有 TTL（存活时间）的（仅存在于内存中）。
 */
public class LocalCacheStore implements CacheStore {

    /** 包含可选过期的 String 值。 */
    private final ConcurrentHashMap<String, Entry> stringStore = new ConcurrentHashMap<>();

    /** 哈希缓存: key → field → value */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> hashStore = new ConcurrentHashMap<>();

    /** 列表缓存: key → list (索引 0 = 左侧 / 头部) */
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> listStore = new ConcurrentHashMap<>();

    /** 集合缓存: key → member 集合 */
    private final ConcurrentHashMap<String, Set<String>> setStore = new ConcurrentHashMap<>();

    /** 有序集合缓存: key → score-升序排序树 (score → member) */
    private final ConcurrentHashMap<String, ConcurrentSkipListMap<Double, String>> zsetStore = new ConcurrentHashMap<>();

    private final ScheduledExecutorService evictScheduler;

    public LocalCacheStore() {
        evictScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "local-cache-evict");
            t.setDaemon(true);
            return t;
        });
        evictScheduler.scheduleAtFixedRate(this::evictExpired, 1, 1, TimeUnit.SECONDS);
    }

    // ---- TTL 驱逐处理 ----

    private void evictExpired() {
        long now = System.currentTimeMillis();
        stringStore.entrySet().removeIf(e -> e.getValue().isExpired(now));
    }

    private static class Entry {
        final String value;
        final long expireAtMs; // -1 = 永远有效

        Entry(String value, long expireAtMs) {
            this.value = value;
            this.expireAtMs = expireAtMs;
        }

        boolean isExpired(long now) {
            return expireAtMs >= 0 && now > expireAtMs;
        }
    }

    // ---- String ----

    @Override
    public void set(String key, String value, long ttl, TimeUnit unit) {
        long expireAtMs = System.currentTimeMillis() + unit.toMillis(ttl);
        stringStore.put(key, new Entry(value, expireAtMs));
    }

    @Override
    public void setForever(String key, String value) {
        stringStore.put(key, new Entry(value, -1));
    }

    @Override
    public Optional<String> get(String key) {
        Entry entry = stringStore.get(key);
        if (entry == null || entry.isExpired(System.currentTimeMillis())) {
            stringStore.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value);
    }

    @Override
    public void delete(String key) {
        stringStore.remove(key);
        hashStore.remove(key);
        listStore.remove(key);
        setStore.remove(key);
        zsetStore.remove(key);
    }

    @Override
    public boolean hasKey(String key) {
        return get(key).isPresent();
    }

    @Override
    public long expire(String key, long ttl, TimeUnit unit) {
        Entry existing = stringStore.get(key);
        if (existing == null) return -1L;
        stringStore.put(key, new Entry(existing.value, System.currentTimeMillis() + unit.toMillis(ttl)));
        return ttl;
    }

    @Override
    public Map<String, Optional<String>> multiGet(List<String> keys) {
        Map<String, Optional<String>> result = new LinkedHashMap<>();
        for (String key : keys) result.put(key, get(key));
        return result;
    }

    // ---- Hash ----

    @Override
    public void hSet(String key, String field, String value) {
        hashStore.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).put(field, value);
    }

    @Override
    public void hSetAll(String key, Map<String, String> entries) {
        hashStore.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).putAll(entries);
    }

    @Override
    public Optional<String> hGet(String key, String field) {
        ConcurrentHashMap<String, String> hash = hashStore.get(key);
        return hash == null ? Optional.empty() : Optional.ofNullable(hash.get(field));
    }

    @Override
    public Map<String, String> hGetAll(String key) {
        ConcurrentHashMap<String, String> hash = hashStore.get(key);
        return hash == null ? Collections.emptyMap() : new LinkedHashMap<>(hash);
    }

    @Override
    public void hDel(String key, String... fields) {
        ConcurrentHashMap<String, String> hash = hashStore.get(key);
        if (hash != null) for (String f : fields) hash.remove(f);
    }

    @Override
    public Long hIncrBy(String key, String field, long delta) {
        ConcurrentHashMap<String, String> hash = hashStore.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        long next = Long.parseLong(hash.getOrDefault(field, "0")) + delta;
        hash.put(field, String.valueOf(next));
        return next;
    }

    // ---- List ----

    @Override
    public Long lPush(String key, String... values) {
        CopyOnWriteArrayList<String> list = listStore.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        for (int i = values.length - 1; i >= 0; i--) list.add(0, values[i]);
        return (long) list.size();
    }

    @Override
    public Long rPush(String key, String... values) {
        CopyOnWriteArrayList<String> list = listStore.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        list.addAll(Arrays.asList(values));
        return (long) list.size();
    }

    @Override
    public List<String> lRange(String key, long start, long end) {
        CopyOnWriteArrayList<String> list = listStore.get(key);
        if (list == null || list.isEmpty()) return Collections.emptyList();
        int size = list.size();
        int from = (int) Math.max(start, 0);
        int to = end < 0 ? size + (int) end + 1 : (int) Math.min(end + 1, size);
        if (from >= to) return Collections.emptyList();
        return new ArrayList<>(list.subList(from, to));
    }

    @Override
    public String lPop(String key) {
        CopyOnWriteArrayList<String> list = listStore.get(key);
        return (list == null || list.isEmpty()) ? null : list.remove(0);
    }

    @Override
    public String rPop(String key) {
        CopyOnWriteArrayList<String> list = listStore.get(key);
        return (list == null || list.isEmpty()) ? null : list.remove(list.size() - 1);
    }

    @Override
    public Long lLen(String key) {
        CopyOnWriteArrayList<String> list = listStore.get(key);
        return list == null ? 0L : (long) list.size();
    }

    // ---- Set ----

    @Override
    public Long sAdd(String key, String... values) {
        Set<String> set = setStore.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        long added = 0;
        for (String v : values) if (set.add(v)) added++;
        return added;
    }

    @Override
    public Set<String> sMembers(String key) {
        Set<String> set = setStore.get(key);
        return set == null ? Collections.emptySet() : new HashSet<>(set);
    }

    @Override
    public Boolean sIsMember(String key, String value) {
        Set<String> set = setStore.get(key);
        return set != null && set.contains(value);
    }

    @Override
    public Long sRem(String key, String... values) {
        Set<String> set = setStore.get(key);
        if (set == null) return 0L;
        long removed = 0;
        for (String v : values) if (set.remove(v)) removed++;
        return removed;
    }

    // ---- ZSet ----

    @Override
    public Boolean zAdd(String key, String value, double score) {
        ConcurrentSkipListMap<Double, String> zset =
                zsetStore.computeIfAbsent(key, k -> new ConcurrentSkipListMap<>());
        zset.put(score, value);
        return true;
    }

    @Override
    public Set<String> zRange(String key, long start, long end) {
        return zSlice(key, start, end, false);
    }

    @Override
    public Set<String> zRevRange(String key, long start, long end) {
        return zSlice(key, start, end, true);
    }

    private Set<String> zSlice(String key, long start, long end, boolean reverse) {
        ConcurrentSkipListMap<Double, String> zset = zsetStore.get(key);
        if (zset == null) return Collections.emptySet();
        List<String> members = reverse
                ? new ArrayList<>(zset.descendingMap().values())
                : new ArrayList<>(zset.values());
        int size = members.size();
        int from = (int) Math.max(start, 0);
        int to = end < 0 ? size + (int) end + 1 : (int) Math.min(end + 1, size);
        if (from >= to || from >= size) return Collections.emptySet();
        return new LinkedHashSet<>(members.subList(from, to));
    }

    @Override
    public Double zScore(String key, String value) {
        ConcurrentSkipListMap<Double, String> zset = zsetStore.get(key);
        if (zset == null) return null;
        return zset.entrySet().stream()
                .filter(e -> Objects.equals(e.getValue(), value))
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }

    @Override
    public Long zRank(String key, String value) {
        ConcurrentSkipListMap<Double, String> zset = zsetStore.get(key);
        if (zset == null) return null;
        long rank = 0;
        for (String member : zset.values()) {
            if (Objects.equals(member, value)) return rank;
            rank++;
        }
        return null;
    }

    @Override
    public Long zRem(String key, String... values) {
        ConcurrentSkipListMap<Double, String> zset = zsetStore.get(key);
        if (zset == null) return 0L;
        Set<String> toRemove = new HashSet<>(Arrays.asList(values));
        long removed = zset.entrySet().stream()
                .filter(e -> toRemove.contains(e.getValue())).count();
        zset.entrySet().removeIf(e -> toRemove.contains(e.getValue()));
        return removed;
    }

    @Override
    public Double zIncrBy(String key, String value, double delta) {
        ConcurrentSkipListMap<Double, String> zset =
                zsetStore.computeIfAbsent(key, k -> new ConcurrentSkipListMap<>());
        Double old = zScore(key, value);
        double next = (old == null ? 0 : old) + delta;
        if (old != null) zset.remove(old);
        zset.put(next, value);
        return next;
    }

    @Override
    public Set<String> zRangeByScore(String key, double min, double max) {
        ConcurrentSkipListMap<Double, String> zset = zsetStore.get(key);
        if (zset == null) return Collections.emptySet();
        return new LinkedHashSet<>(zset.subMap(min, true, max, true).values());
    }
}
