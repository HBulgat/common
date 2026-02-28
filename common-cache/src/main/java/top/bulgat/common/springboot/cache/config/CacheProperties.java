package top.bulgat.common.springboot.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * common-cache 的配置属性。
 *
 * <pre>
 * common:
 *   cache:
 *     null-value-ttl: 60          # 空值标记在缓存中停留的秒数（防穿透）
 *     jitter-factor: 0.2          # ±20% 随机 TTL 波动（防雪崩），0 = 禁用
 *     rebuild-executor-size: 4    # 异步逻辑过期重建的线程数（防击穿）
 * </pre>
 */
@ConfigurationProperties(prefix = "common.cache")
public class CacheProperties {

    /** 空值条目（防止缓存穿透）的 TTL 秒数。 */
    private long nullValueTtl = 60;

    /**
     * 作为随机波动的 TTL 比例（防止缓存雪崩）。
     * 例如，0.2 意味着实际 TTL = 配置的 TTL × (1 ± random(0, 0.2))。
     * 设为 0 可禁用随机波动。
     */
    private double jitterFactor = 0.2;

    /** 异步逻辑过期缓存重建的线程池大小。 */
    private int rebuildExecutorSize = 4;

    public long getNullValueTtl() { return nullValueTtl; }
    public void setNullValueTtl(long nullValueTtl) { this.nullValueTtl = nullValueTtl; }

    public double getJitterFactor() { return jitterFactor; }
    public void setJitterFactor(double jitterFactor) { this.jitterFactor = jitterFactor; }

    public int getRebuildExecutorSize() { return rebuildExecutorSize; }
    public void setRebuildExecutorSize(int rebuildExecutorSize) { this.rebuildExecutorSize = rebuildExecutorSize; }
}
