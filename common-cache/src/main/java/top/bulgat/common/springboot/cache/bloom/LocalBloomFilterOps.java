package top.bulgat.common.springboot.cache.bloom;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.nio.charset.StandardCharsets;

/**
 * 基于内存 (Guava) 的布隆过滤器实现。
 * 适用于单机、或无需严格全局协调的防穿透拦截。
 * <p>
 * 注意：使用前需确保项目中引入了 Guava 依赖。
 *
 * @param <ID> 业务主键类型
 */
public class LocalBloomFilterOps<ID> implements BloomFilterOps<ID> {

    private final BloomFilter<CharSequence> bloomFilter;

    /**
     * 构造本地布隆过滤器。
     *
     * @param expectedInsertions 预期的插入数据量
     * @param fpp 期望的误判率 (False Positive Probability, 如 0.01)
     */
    public LocalBloomFilterOps(long expectedInsertions, double fpp) {
        this.bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                expectedInsertions,
                fpp
        );
    }

    @Override
    public boolean mightContain(ID id) {
        if (id == null) return false;
        return bloomFilter.mightContain(String.valueOf(id));
    }

    @Override
    public void put(ID id) {
        if (id != null) {
            bloomFilter.put(String.valueOf(id));
        }
    }
}
