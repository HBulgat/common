package top.bulgat.common.springboot.cache;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存数据加载器 (Cache Loader) 用户实现接口。
 * 定义了针对特定业务实体的以下行为：
 * <ul>
 *   <li>{@link #key(Object)} — 如何根据业务 ID 生成唯一的缓存键 (Redis Key)</li>
 *   <li>{@link #load(Object)} — 在缓存未命中 (Miss) 时，如何从数据源 (如数据库/RPC) 加载单条数据</li>
 *   <li>{@link #multiLoad(List)} — 在批量缓存未命中时，如何批量加载数据（强烈建议覆写为底层的 IN 查询，防御 N+1 问题）</li>
 * </ul>
 *
 * <p>本模块的框架门面 ({@link CacheTemplate}) 会完全接管缓存编排：
 * 自动查找缓存、在未命中时调用本接口查询 DB、自动实施防穿透/击穿策略、自动计算防雪崩抖动 TTL 并写回缓存。
 *
 * <p><b>扩展点 (Plan B 高级架构):</b> 覆写 {@link #storeOps()} 可以返回自定义的额外操作对象，
 * 用于完全接管 String 之外的缓存数据存储结构 (如 Hash, ZSet, List, Set)。
 *
 * @param <ID> 业务主键标识符的类型
 * @param <V>  业务缓存值的类型
 */
public interface CacheLoader<ID, V> {

    /**
     * 【定义缓存键】根据传入的业务 ID 构造该实体在 Redis 或本地缓存中的绝对 Key。
     * 建议包含业务前缀，如 {@code "user:info:" + id}。
     */
    String key(ID id);

    /**
     * 【单条加载】当缓存未命中时，框架会强制调用本方法从最终数据源提取单条数据。
     * <p>防穿透处理支持：如果数据在数据库中也确实不存在，允许且应该返回 {@code null}，
     * 框架接收到 {@code null} 后会自动在缓存中写入短暂有效期的空值标记拦截后续穿透请求。
     *
     * @param id 业务查库主键标示
     * @return 业务源数据，不存在则返回 null
     */
    V load(ID id);

    /**
     * 【批量加载】当进行批量缓存查询且存在未命中项时，框架会将缺失的 ID 列表传入调用本方法。
     * <p>
     * <b>默认实现</b>: 依次循环调用单条 {@link #load(Object)}（引发极其低效的 N 次单条查询）。
     * <b>强烈建议覆写</b>: 在实际业务类中将该方法重写为调用类似 MyBatis Plus 的 {@code listByIds()} 单次批量 IN 查询。
     *
     * @param ids 在缓存中未命中数据的缺失 ID 列表
     * @return 返回真实存在数据的 id -> value 映射；对于数据库中确实查不到的 ID 直接忽略（无需加入 map，框架会依据返回结果自动兜底处理防穿天空值标记）
     */
    default Map<ID, V> multiLoad(List<ID> ids) {
        Map<ID, V> result = new LinkedHashMap<>();
        for (ID id : ids) {
            V v = load(id);
            if (v != null) {
                result.put(id, v);
            }
        }
        return result;
    }

    /**
     * 【基础 TTL】该业务实体的默认存活时间数值。
     * 框架写缓存时会在该数值基础上直接叠加随机抖动机制防止雪崩。
     */
    default long ttl() {
        return 30;
    }

    /**
     * 【基础 TTL 时间单位】防雪崩基础时间的度量单位（默认为分钟）。
     */
    default TimeUnit ttlUnit() {
        return TimeUnit.MINUTES;
    }

    /**
     * 【Plan B 结构扩展】声明当前 Loader 将使用哪种缓存存储策略及结构进行落地。
     * <p>
     * <b>默认行为</b>: 返回 null，框架在运行时会自动将其解析为 {@link StringCacheStoreOps}，
     * 使用基于 Jackson 的 JSON 序列化并存入最常规的 Redis String 类型中。
     * <p>
     * <b>覆写场景</b>: 若要利用 Hash 的 field 过期、或使用 ZSet 排名等任何复杂非 String 结构，请重写此方法返回特定的存储拦截器操作。
     */
    default CacheStoreOps<V> storeOps() {
        return null; // resolved to StringCacheStoreOps by CacheTemplate at runtime
    }
}
