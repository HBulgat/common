package top.bulgat.common.springboot.cache;

/**
 * Represents the result of reading from the cache.
 * Distinguishes between a true miss (data not in cache) and a hit
 * (which could hold a null value due to anti-penetration).
 */
public class CacheHit<V> {
    private final boolean hit;
    private final V value;

    private CacheHit(boolean hit, V value) {
        this.hit = hit;
        this.value = value;
    }

    public static <V> CacheHit<V> miss() {
        return new CacheHit<>(false, null);
    }

    public static <V> CacheHit<V> hit(V value) {
        return new CacheHit<>(true, value);
    }

    public boolean isHit() {
        return hit;
    }

    public V getValue() {
        return value;
    }
}
