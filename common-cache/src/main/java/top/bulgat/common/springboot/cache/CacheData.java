package top.bulgat.common.springboot.cache;

import java.time.LocalDateTime;

/**
 * 缓存在系统中的数据包装类，用于实现“逻辑过期”防击穿策略。
 * <p>
 * 缓存键本身不设置真实 TTL（永久有效）。过期时间在该包装类内部跟踪。
 * 当请求发现数据已逻辑过期时，会立即返回旧值，并在后台触发异步重建。
 *
 * @param <V> 缓存的业务值类型
 */
public class CacheData<V> {

    private V data;

    private LocalDateTime expireAt;

    public CacheData() {
    }

    public CacheData(V data, LocalDateTime expireAt) {
        this.data = data;
        this.expireAt = expireAt;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireAt);
    }

    public V getData() { return data; }
    public void setData(V data) { this.data = data; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
}
