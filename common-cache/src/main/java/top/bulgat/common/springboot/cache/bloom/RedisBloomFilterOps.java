package top.bulgat.common.springboot.cache.bloom;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;

/**
 * 基于 Redis (Redisson) 的分布式布隆过滤器实现。
 * 适用于微服务集群下的全局统一防穿透拦截。
 * <p>
 * 注意：使用前需确保项目中引入了 Redisson 依赖且环境可用。
 *
 * @param <ID> 业务主键类型
 */
public class RedisBloomFilterOps<ID> implements BloomFilterOps<ID> {

    private final RBloomFilter<Object> bloomFilter;

    /**
     * 构造分布式 Redis 布隆过滤器。
     * 
     * @param redissonClient Redisson 客户端
     * @param filterName 在 Redis 中存储此布隆过滤器的键名称
     * @param expectedInsertions 预期的总插入量
     * @param fpp 期望的误判率 (如 0.01)
     */
    public RedisBloomFilterOps(RedissonClient redissonClient, String filterName, long expectedInsertions, double fpp) {
        this.bloomFilter = redissonClient.getBloomFilter(filterName);
        // 如果此前并未初始化，则按给定的容量和误判率在 Redis 中初始化布隆过滤器
        this.bloomFilter.tryInit(expectedInsertions, fpp);
    }

    @Override
    public boolean mightContain(ID id) {
        if (id == null) return false;
        return bloomFilter.contains(id);
    }

    @Override
    public void put(ID id) {
        if (id != null) {
            bloomFilter.add(id);
        }
    }
}
