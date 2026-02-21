package top.bulgat.common.springboot.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.bulgat.common.springboot.cache.config.CacheProperties;
import top.bulgat.common.springboot.cache.store.LocalCacheStore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CacheTemplate} using {@link LocalCacheStore} to verify strategy flows.
 */
public class CacheTemplateTest {

    private CacheTemplate template;
    private CacheStore store;
    private CacheProperties properties;

    @BeforeEach
    void setUp() {
        store = new LocalCacheStore();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // for LocalDateTime

        properties = new CacheProperties();
        properties.setNullValueTtl(60);
        properties.setJitterFactor(0); // Disable jitter in test for predictable assertions
        
        template = new CacheTemplate(store, mapper, properties);
    }

    static class TestUser {
        public String name;
        public int age;
        public TestUser() {}
        public TestUser(String name, int age) { this.name = name; this.age = age; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestUser testUser = (TestUser) o;
            return age == testUser.age && name.equals(testUser.name);
        }
    }

    @Test
    void testPassThroughAndCacheHit() {
        int[] loadCount = {0};

        CacheLoader<Long, TestUser> loader = new CacheLoader<>() {
            @Override
            public String key(Long id) { return "u:" + id; }
            @Override
            public TestUser load(Long id) {
                loadCount[0]++;
                if (id == 999L) return null; // simulate Miss
                return new TestUser("Alice", 20);
            }
        };

        // First read: DB load (count = 1)
        TestUser u = template.getWithPassThroughNullMarker(1L, TestUser.class, loader);
        assertNotNull(u);
        assertEquals("Alice", u.name);
        assertEquals(1, loadCount[0]);

        // Second read: Cache hit (count = 1)
        TestUser u2 = template.getWithPassThroughNullMarker(1L, TestUser.class, loader);
        assertNotNull(u2);
        assertEquals(1, loadCount[0]);

        // First read non-existent: DB load returns null (count = 2)
        TestUser notFound = template.getWithPassThroughNullMarker(999L, TestUser.class, loader);
        assertNull(notFound);
        assertEquals(2, loadCount[0]);

        // Second read non-existent: Cache hit on null marker (count = 2)
        assertNull(template.getWithPassThroughNullMarker(999L, TestUser.class, loader));
        assertEquals(2, loadCount[0]); // Did not hit DB
    }

    @Test
    void testLogicalExpiry() throws InterruptedException {
        java.util.concurrent.atomic.AtomicInteger loadCount = new java.util.concurrent.atomic.AtomicInteger(0);

        CacheLoader<Long, TestUser> loader = new CacheLoader<>() {
            @Override
            public String key(Long id) { return "u:" + id; }
            @Override
            public TestUser load(Long id) {
                int count = loadCount.incrementAndGet();
                return new TestUser("Bob_v" + count, 30);
            }
            @Override
            public long ttl() { return 1; } // 1 unit logical TTL
            @Override
            public java.util.concurrent.TimeUnit ttlUnit() { return java.util.concurrent.TimeUnit.SECONDS; }
        };

        // Must warm up first
        template.warmUp(2L, loader);
        assertEquals(1, loadCount.get());

        // Read immediately: should be valid
        TestUser u1 = template.getWithLogicalExpiry(2L, TestUser.class, loader);
        assertEquals("Bob_v1", u1.name);

        // Wait for logical expiry
        Thread.sleep(1100);

        // Read after logical expiry: should return stale ("Bob_v1") and trigger async rebuild
        TestUser u2 = template.getWithLogicalExpiry(2L, TestUser.class, loader);
        assertEquals("Bob_v1", u2.name);

        // Wait for async rebuild to complete
        Thread.sleep(2000);

        // Subsequent read: should return updated ("Bob_v2")
        TestUser u3 = template.getWithLogicalExpiry(2L, TestUser.class, loader);
        assertEquals("Bob_v2", u3.name);
        assertEquals(2, loadCount.get());
    }

    @Test
    void testMultiGet() {
        int[] multiLoadCount = {0};
        
        CacheLoader<Long, TestUser> loader = new CacheLoader<>() {
            @Override
            public String key(Long id) { return "u:" + id; }
            @Override
            public TestUser load(Long id) { throw new UnsupportedOperationException(); }
            @Override
            public Map<Long, TestUser> multiLoad(List<Long> ids) {
                multiLoadCount[0]++;
                Map<Long, TestUser> map = new HashMap<>();
                for (Long id : ids) {
                    if (id != 999L) map.put(id, new TestUser("Name" + id, id.intValue()));
                }
                return map;
            }
        };

        // Manually place ID 1 into cache to simulate partial hit
        template.put(1L, new TestUser("Existing1", 100), loader);

        // Batch get: 1 (hit), 2 (miss), 999 (miss/not found)
        Map<Long, TestUser> res = template.multiGetWithPassThroughNullMarker(Arrays.asList(1L, 2L, 999L), TestUser.class, loader);
        
        assertEquals(1, multiLoadCount[0]); // Single call for misses (2L, 999L)
        assertEquals(2, res.size());
        assertEquals("Existing1", res.get(1L).name); // Cached value retrieved
        assertEquals("Name2", res.get(2L).name);     // DB loaded
        assertNull(res.get(999L));                   // DB not found

        // Second batch get: All should hit cache (or null marker)
        Map<Long, TestUser> res2 = template.multiGetWithPassThroughNullMarker(Arrays.asList(1L, 2L, 999L), TestUser.class, loader);
        assertEquals(1, multiLoadCount[0]); // DB skipped!
        assertEquals(2, res2.size());
    }

    @Test
    void testGetWithPassThroughBloomFilter() {
        int[] loadCount = {0};
        CacheLoader<Long, TestUser> loader = new CacheLoader<>() {
            @Override
            public String key(Long id) { return "u:" + id; }
            @Override
            public TestUser load(Long id) {
                loadCount[0]++;
                return id == 999L ? null : new TestUser("Alice", 25);
            }
        };

        // Standard pass-through for existing/non-existing keys
        top.bulgat.common.springboot.cache.bloom.BloomFilterOps<Long> filter = new top.bulgat.common.springboot.cache.bloom.LocalBloomFilterOps<>(1000, 0.01);
        filter.put(1L);
        // We do not put 999L in filter, meaning it should be rejected immediately by bloom filter
        
        TestUser u = template.getWithPassThroughBloomFilter(1L, TestUser.class, loader, filter);
        assertNotNull(u);
        assertEquals(1, loadCount[0]);

        TestUser notFound = template.getWithPassThroughBloomFilter(999L, TestUser.class, loader, filter);
        assertNull(notFound);
        assertEquals(1, loadCount[0]); // DB skipped immediately due to bloom filter
    }

    @Test
    void testMultiGetWithLogicalExpiry() throws InterruptedException {
        int[] multiLoadCount = {0};
        CacheLoader<Long, TestUser> loader = new CacheLoader<>() {
            @Override
            public String key(Long id) { return "u:" + id; }
            @Override
            public TestUser load(Long id) { return null; }
            @Override
            public java.util.Map<Long, TestUser> multiLoad(java.util.List<Long> ids) {
                multiLoadCount[0]++;
                java.util.Map<Long, TestUser> map = new java.util.HashMap<>();
                for (Long id : ids) {
                    map.put(id, new TestUser("Bob_v" + multiLoadCount[0], Math.toIntExact(id)));
                }
                return map;
            }
            @Override
            public long ttl() { return 1; } // 1 second logical TTL
            @Override
            public java.util.concurrent.TimeUnit ttlUnit() { return java.util.concurrent.TimeUnit.SECONDS; }
        };

        java.util.List<Long> ids = Arrays.asList(1L, 2L);
        // Warm up
        template.multiWarmUp(ids, loader);
        assertEquals(1, multiLoadCount[0]);

        // Get immediately -> should hit logically valid cache
        java.util.Map<Long, TestUser> res1 = template.multiGetWithLogicalExpiry(ids, TestUser.class, loader);
        assertEquals("Bob_v1", res1.get(1L).name);
        
        // Wait for logical expiry
        Thread.sleep(1100);
        
        // Get after expiry -> returns stale data immediately, triggers async rebuild in background
        java.util.Map<Long, TestUser> res2 = template.multiGetWithLogicalExpiry(ids, TestUser.class, loader);
        assertEquals("Bob_v1", res2.get(1L).name);

        // Wait to allow async rebuild to finish
        Thread.sleep(2000);
        
        // Get again -> should now get updated data
        java.util.Map<Long, TestUser> res3 = template.multiGetWithLogicalExpiry(ids, TestUser.class, loader);
        assertEquals("Bob_v2", res3.get(1L).name);
        assertEquals(2, multiLoadCount[0]);
    }
}
