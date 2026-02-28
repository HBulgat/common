# common-cache 模块说明

本模块提供了一个统一的、健壮的高可用缓存中间件门面封装。它屏蔽了底层具体存储（如 Redis 或本地 ConcurrentHashMap），通过暴露一套标准化的高层策略，内建了解决缓存三大核心问题（穿透、雪崩、击穿）的能力。

## 架构和使用理念

业务代码中**不推荐**直接注入并且使用底层的 `StringRedisTemplate` 或 `RedisCacheStore` 等执行诸如 String/JSON 的基础查询或者增删改查。直接使用底层缓存很容易产生缓存穿透和缓存击穿的重大风险，需要写大量冗余并且易错的安全防御代码。

更推荐的做法是：
1. 业务对象实现 `CacheLoader<ID, Value>` 接口，描述“键如何映射”以及“如何从底层 DB/微服务加载数据”。
2. 将实现了的 loader 结合主键 ID 传递并调用核心门面 `CacheTemplate` 的对应策略方法（如 `getWithPassThroughNullMarker`）。

## 核心类介绍与使用

### 1. `CacheTemplate`
作为用户的核心入口，提供了各种健壮的缓存获取策略。

#### 核心方法说明 & 解决的问题：
- **`getWithPassThroughNullMarker`**（防止缓存穿透）：
  试图从缓存获取对象。如果底层缓存和 DB 中都查不到该对象，它会在缓存中放入一个生存期很短的“空值标记（Null-Marker）”，并在接下来的时间内直接在缓存层拦截击穿 DB 的恶意请求。
  **使用示例**：
  ```java
  User user = cacheTemplate.getWithPassThroughNullMarker(userId, User.class, userCacheLoader);
  ```

- **`warmUp` & `getWithLogicalExpiry`**（防止缓存击穿）：
  对于超热点数据，我们不能让它在到达物理 TTL 的一瞬间瞬间失效而导致成千上万并发请求击穿 DB。
  - 使用 `warmUp`：主动用一个没有真实 TTL 的包装 (`CacheData`) 把数据预热进内存，包装内部附带一个逻辑过期时刻。
  - 使用 `getWithLogicalExpiry`：查询时，如果数据没有物理过期，但是内部时间显示已逻辑过期，方法会立刻返回旧数据（不阻塞当前请求），然后将重建缓存（查 DB 并刷回 Redis）的动作甩进后台异步线程持池。
  
- **`multiGetWithPassThroughNullMarker` & `multiGetWithLogicalExpiry`**（批量防穿透与批量防击穿）：
  同上策略的批量查询版本。它能够接收 `List<ID>`，通过底层的 MGET 合并读取大幅度减少网络 RTT。强烈建议 `CacheLoader` 里的 `multiLoad` 结合 MyBatis Plus 的一次性 IN 语句从而防止 N+1 查询。
 
- **基础写与驱逐**：
  - `put(id, val, loader)` / `multiPut(idValMap, loader)` 主动写缓存并自动应用 TTL 抖动。
  - `invalidate(id, loader)` 从缓存驱逐对应键值。
  
> **提示 (防缓存雪崩)**：`CacheTemplate` 为一切基于 `CacheLoader` 定义的自动或手动写入操作注入“随机 TTL 抖动(Jitter)”，保证所有同批存入缓存的数据不会在完全一致的时间点集体失效。

### 2. `CacheLoader<ID, V>`
用于向 `CacheTemplate` 提供如何处理未命中的回调接口。推荐在服务层使用匿名内部类或者私有内部类实现。

**重要方法：**
- `String key(ID id)`: 根据传入 ID 生成缓存中实际使用的键名，例如 `"user:info:" + id`。
- `V load(ID id)`: 如果彻底未命中缓存，需要如何返回单个实例对象（可返回 NULL 用于触发防穿透）。
- `Map<ID, V> multiLoad(List<ID> ids)`: 可选重写。处理批量查下时的兜底查库，非常推荐重写为一条数据库 IN 查询。
- `long ttl()` / `TimeUnit ttlUnit()`: 定义业务存活时间基准值。

**使用示例：**
```java
private final CacheLoader<Long, User> userCacheLoader = new CacheLoader<Long, User>() {
    @Override
    public String key(Long id) { return "app:user:" + id; }
    @Override
    public User load(Long id) { return userMapper.selectById(id); }
    // 覆盖 multiLoad 避免 for 循环带来的 N+1
    @Override
    public Map<Long, User> multiLoad(List<Long> ids) {
       return userMapper.selectBatchIds(ids).stream()
           .collect(Collectors.toMap(User::getId, u -> u));
    }
    @Override
    public long ttl() { return 60; }
};
```

### 3. 数据层基建：`CacheStore` / `RedisCacheStore` / `LocalCacheStore`
这是屏蔽底层实现的键值抽象。系统启动时会根据是否依赖 `StringRedisTemplate` 自动按需实例化 `RedisCacheStore`。
如果你需要编写 Redis 原生专属的数据结构操作（而并非缓存 JSON），比如排行榜（ZSet），访问计数（Hash, Incr），可以直接向 Spring 注入 `CacheStore` 从而调用 `store.lPush()`, `store.hSet()`, `store.zRange()`。

### 4. 扩展机制：`CacheStoreOps` 及其实现
默认下所有的 POJO 数据都是利用 Jackson 转化为 JSON 并利用 `StringCacheStoreOps` 操作存储进底层中间件。
此接口留作高级扩展，用以将 `CacheData` 等封装结构拆解存储到 Hash, Set 等非字符串模型下（Plan B）。

### 5. `BloomFilterOps` (布隆防穿透)
针对极端极端的穿透黑客，当防穿透 Null-Marker 需要占用海量短时内存时，可以使用预先计算的容错位图（布隆过滤器）。
- `LocalBloomFilterOps`: 基于单机 Guava 的位图支持。
- `RedisBloomFilterOps`: 基于 Redis 和 Redisson，支持分布式微服务多节点数据探查（前提是需要自行额外添加并配置 RedissonClient 提供者）。
利用 `cacheTemplate.getWithPassThroughBloomFilter(id, clazz, loader, bloomOps)` 可拦截绝大部分荒谬的传入 ID。

### 6. 辅助/自动配置类
- **`CacheData<V>`**: `warmUp` 时使用的带有内部逻辑实效时间的包装体。
- **`CacheHit<V>`**: 用以表示读取状态（可分辨是否摸到了 Null-Marker）。
- **`CacheProperties`**: 结合配置字典定义防雪崩的百分比 (`jitter-factor`)，驱逐线程，空值时间戳等。
- **`CacheAutoConfiguration`**: 为 Spring Web Starter 提供全部基建 Bean 的一键装配和注入。
