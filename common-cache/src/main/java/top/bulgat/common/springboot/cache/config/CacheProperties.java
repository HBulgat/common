package top.bulgat.common.springboot.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for common-cache.
 *
 * <pre>
 * common:
 *   cache:
 *     null-value-ttl: 60          # seconds a null-marker stays in cache (防穿透)
 *     jitter-factor: 0.2          # ±20% random TTL spread (防雪崩), 0 = disabled
 *     rebuild-executor-size: 4    # threads for async logical-expiry rebuild (防击穿)
 * </pre>
 */
@ConfigurationProperties(prefix = "common.cache")
public class CacheProperties {

    /** TTL in seconds for null-marker entries (cache penetration prevention). */
    private long nullValueTtl = 60;

    /**
     * Fraction of TTL to use as random jitter (cache avalanche prevention).
     * E.g. 0.2 means actual TTL = configured TTL × (1 ± random(0, 0.2)).
     * Set to 0 to disable jitter.
     */
    private double jitterFactor = 0.2;

    /** Thread pool size for async logical-expiry cache rebuild. */
    private int rebuildExecutorSize = 4;

    public long getNullValueTtl() { return nullValueTtl; }
    public void setNullValueTtl(long nullValueTtl) { this.nullValueTtl = nullValueTtl; }

    public double getJitterFactor() { return jitterFactor; }
    public void setJitterFactor(double jitterFactor) { this.jitterFactor = jitterFactor; }

    public int getRebuildExecutorSize() { return rebuildExecutorSize; }
    public void setRebuildExecutorSize(int rebuildExecutorSize) { this.rebuildExecutorSize = rebuildExecutorSize; }
}
