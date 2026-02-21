package top.bulgat.common.springboot.cache.bloom;

/**
 * 布隆过滤器操作接口（Bloom Filter Ops）。
 * 用于【防缓存穿透】的方案 B：利用布隆过滤器拦截绝对不存在的 Key。
 *
 * @param <ID> 业务主键标识符的类型
 */
public interface BloomFilterOps<ID> {

    /**
     * 判断给定的 ID 是否可能存在。
     * 
     * @param id 业务标示
     * @return 如果返回 false，代表该 ID 绝对不存在；如果返回 true，代表可能存在（有微小的误判率）。
     */
    boolean mightContain(ID id);

    /**
     * 将给定的 ID 放入布隆过滤器。
     * 
     * @param id 业务标示
     */
    void put(ID id);
}
