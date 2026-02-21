package top.bulgat.common.springboot.cache.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.bulgat.common.springboot.cache.CacheStore;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class LocalCacheStoreTest {

    private CacheStore store;

    @BeforeEach
    void setUp() {
        store = new LocalCacheStore();
    }

    @Test
    void testStringOperations() throws InterruptedException {
        String key = "str:key";
        store.set(key, "val", 1, TimeUnit.SECONDS);
        assertTrue(store.hasKey(key));
        assertEquals("val", store.get(key).orElse(null));

        // Test TTL eviction
        Thread.sleep(1500);
        assertFalse(store.hasKey(key));
        assertTrue(store.get(key).isEmpty());

        // Test setForever
        store.setForever("forever", "val2");
        assertTrue(store.hasKey("forever"));

        // Test delete
        store.delete("forever");
        assertFalse(store.hasKey("forever"));

        // Test multiGet
        store.setForever("k1", "v1");
        store.setForever("k2", "v2");
        Map<String, Optional<String>> res = store.multiGet(Arrays.asList("k1", "k2", "k3"));
        assertEquals("v1", res.get("k1").orElse(null));
        assertEquals("v2", res.get("k2").orElse(null));
        assertTrue(res.get("k3").isEmpty());
    }

    @Test
    void testHashOperations() {
        String key = "hash:key";
        store.hSet(key, "f1", "v1");
        assertEquals("v1", store.hGet(key, "f1").orElse(null));

        Map<String, String> entries = new HashMap<>();
        entries.put("f2", "v2");
        entries.put("f3", "v3");
        store.hSetAll(key, entries);

        Map<String, String> all = store.hGetAll(key);
        assertEquals(3, all.size());
        assertEquals("v1", all.get("f1"));
        assertEquals("v2", all.get("f2"));

        store.hDel(key, "f1", "f2");
        assertFalse(store.hGet(key, "f1").isPresent());

        store.hIncrBy(key, "count", 5);
        assertEquals("5", store.hGet(key, "count").orElse(null));
        store.hIncrBy(key, "count", -2);
        assertEquals("3", store.hGet(key, "count").orElse(null));
    }

    @Test
    void testListOperations() {
        String key = "list:key";
        store.rPush(key, "1", "2", "3");
        assertEquals(3, store.lLen(key));

        store.lPush(key, "0");
        assertEquals(4, store.lLen(key));

        // List is now: 0, 1, 2, 3
        assertEquals("0", store.lPop(key));
        assertEquals("3", store.rPop(key));

        // List is now: 1, 2
        List<String> range = store.lRange(key, 0, -1);
        assertEquals(Arrays.asList("1", "2"), range);
    }

    @Test
    void testSetOperations() {
        String key = "set:key";
        store.sAdd(key, "a", "b", "c");
        assertTrue(store.sIsMember(key, "a"));
        assertFalse(store.sIsMember(key, "x"));

        Set<String> members = store.sMembers(key);
        assertEquals(3, members.size());
        assertTrue(members.containsAll(Arrays.asList("a", "b", "c")));

        store.sRem(key, "b", "c");
        assertEquals(1, store.sMembers(key).size());
    }

    @Test
    void testZSetOperations() {
        String key = "zset:key";
        store.zAdd(key, "m1", 10.0);
        store.zAdd(key, "m2", 20.0);
        store.zAdd(key, "m3", 15.0);

        assertEquals(10.0, store.zScore(key, "m1"));
        assertEquals(15.0, store.zScore(key, "m3"));

        // Ordered by score ascending
        Set<String> range = store.zRange(key, 0, -1);
        assertArrayEquals(new String[]{"m1", "m3", "m2"}, range.toArray());

        Set<String> revRange = store.zRevRange(key, 0, -1);
        assertArrayEquals(new String[]{"m2", "m3", "m1"}, revRange.toArray());

        store.zIncrBy(key, "m1", 15.0); // m1 becomes 25.0
        Set<String> newRev = store.zRevRange(key, 0, -1);
        assertArrayEquals(new String[]{"m1", "m2", "m3"}, newRev.toArray());

        store.zRem(key, "m2");
        assertEquals(null, store.zScore(key, "m2"));
    }
}
