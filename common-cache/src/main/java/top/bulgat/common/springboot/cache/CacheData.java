package top.bulgat.common.springboot.cache;

import java.time.LocalDateTime;

/**
 * Wrapper stored in the cache for the "logical expiry" anti-breakdown strategy.
 * <p>
 * The key is set with NO real TTL (permanent). Expiry is tracked inside this wrapper.
 * When a request finds the data logically expired, it returns the stale value immediately
 * and triggers an async rebuild in the background.
 *
 * @param <V> type of the cached business value
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
