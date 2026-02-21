package top.bulgat.common.springboot.cache.store;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.bulgat.common.springboot.cache.CacheStore;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test connecting to a real Redis instance.
 */
public class RedisCacheStoreIntegrationTest {

    private static LettuceConnectionFactory factory;
    private static CacheStore store;
    private static StringRedisTemplate redisTemplate;

    @BeforeAll
    static void setUpAll() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("redis.bulgat.top", 6379);
        config.setPassword("h3104261648");

        factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();

        redisTemplate = new StringRedisTemplate(factory);
        redisTemplate.afterPropertiesSet();

        store = new RedisCacheStore(redisTemplate);

        // Pre-clean test keys
        cleanUp();
    }

    @AfterAll
    static void tearDownAll() {
        cleanUp();
        if (factory != null) {
            factory.destroy();
        }
    }

    private static void cleanUp() {
        Set<String> keys = redisTemplate.keys("test:cache:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void testStringOperations() {
        String key = "test:cache:str:key1";
        store.set(key, "val1", 5, TimeUnit.MINUTES);
        
        assertTrue(store.hasKey(key));
        assertEquals("val1", store.get(key).orElse(null));

        store.delete(key);
        assertFalse(store.hasKey(key));

        store.setForever(key, "forever");
        assertEquals("forever", store.get(key).orElse(null));

        store.expire(key, 1, TimeUnit.SECONDS);
        try { Thread.sleep(1500); } catch (InterruptedException e) {}
        assertFalse(store.hasKey(key));

        store.setForever("test:cache:m:1", "v1");
        store.setForever("test:cache:m:2", "v2");
        Map<String, Optional<String>> res = store.multiGet(Arrays.asList("test:cache:m:1", "test:cache:m:2", "test:cache:m:notfound"));
        assertEquals("v1", res.get("test:cache:m:1").orElse(null));
        assertTrue(res.get("test:cache:m:notfound").isEmpty());
    }

    @Test
    void testHashOperations() {
        String key = "test:cache:hash:key";
        store.hSet(key, "f1", "v1");
        assertEquals("v1", store.hGet(key, "f1").orElse(null));

        Map<String, String> entries = new HashMap<>();
        entries.put("f2", "v2");
        entries.put("f3", "v3");
        store.hSetAll(key, entries);

        Map<String, String> all = store.hGetAll(key);
        assertEquals(3, all.size());
        assertEquals("v2", all.get("f2"));

        store.hDel(key, "f1");
        assertFalse(store.hGet(key, "f1").isPresent());

        store.hIncrBy(key, "count", 10);
        assertEquals("10", store.hGet(key, "count").orElse(null));
    }

    @Test
    void testListOperations() {
        String key = "test:cache:list:key";
        store.rPush(key, "a", "b", "c");
        assertEquals(3, store.lLen(key));

        store.lPush(key, "x");
        assertEquals(4, store.lLen(key));

        // Result should be x, a, b, c
        assertEquals("x", store.lPop(key));
        assertEquals("c", store.rPop(key));

        List<String> range = store.lRange(key, 0, -1);
        assertEquals(Arrays.asList("a", "b"), range);
    }

    @Test
    void testZSetOperations() {
        String key = "test:cache:zset:key";
        store.zAdd(key, "m1", 100.0);
        store.zAdd(key, "m2", 50.0);
        store.zAdd(key, "m3", 75.0);

        assertEquals(100.0, store.zScore(key, "m1"));

        Set<String> range = store.zRange(key, 0, -1);
        assertArrayEquals(new String[]{"m2", "m3", "m1"}, range.toArray());

        Set<String> revRange = store.zRevRange(key, 0, -1);
        assertArrayEquals(new String[]{"m1", "m3", "m2"}, revRange.toArray());

        store.zIncrBy(key, "m2", 100.0); // m2 -> 150.0
        Set<String> updatedRev = store.zRevRange(key, 0, -1);
        assertArrayEquals(new String[]{"m2", "m1", "m3"}, updatedRev.toArray());
    }
}
