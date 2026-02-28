package top.bulgat.common.springboot.cache;

/**
 * 表示从缓存读取的结果。
 * 区分真实的未命中（缓存中无数据）和命中
 * （由于防穿透机制可能包含 null 值）。
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
